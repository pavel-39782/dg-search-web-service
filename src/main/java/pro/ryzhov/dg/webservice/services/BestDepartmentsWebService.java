package pro.ryzhov.dg.webservice.services;

import com.google.gson.*;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import pro.ryzhov.dg.webservice.beans.BestDepartmentsResponse;
import pro.ryzhov.dg.webservice.beans.DepartmentData;
import pro.ryzhov.dg.webservice.beans.ErrorResponse;
import pro.ryzhov.dg.webservice.gson.DepartmentDataTypeAdapter;
import spark.Response;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

import static spark.Spark.get;

public class BestDepartmentsWebService {

    private static final double API_VERSION = 1.0;

    private static final CloseableHttpClient HTTP_CLIENT;
    private static final Gson GSON;
    private static final ExecutorService EXECUTOR_SERVICE;
    private static final List<String> TOWNS_TO_CHECK;

    private static final String DOUBLEGIS_KEY = "ruuxah6217";
    private static final String DOUBLEGIS_API_VERSION = "1.3";

    private static final String URI_SCHEMA = "http";
    private static final String URI_HOST = "catalog.api.2gis.ru";
    private static final String SEARCH_URI_PATH = "/search";
    private static final String PROFILE_URI_PATH = "/profile";

    private static final String SEARCH_QUERY_TEMPLATE = "key=" + DOUBLEGIS_KEY
            + "&version=" + DOUBLEGIS_API_VERSION
            + "&sort=rating"
            + "&pagesize=5" // 5 is the lowest pagesize allowed value for 2GIS API v1.3
            + "&where=%s"
            + "&what=%s";
    private static final String PROFILE_QUERY_TEMPLATE = "key=" + DOUBLEGIS_KEY
            + "&version=" + DOUBLEGIS_API_VERSION
            + "&id=%s"
            + "&hash=%s";

    static {
        HTTP_CLIENT = HttpClients.custom()
                .setConnectionManager(new PoolingHttpClientConnectionManager())
                .build();
        GSON = new GsonBuilder()
                .setPrettyPrinting()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(DepartmentData.class, new DepartmentDataTypeAdapter())
                .create();
        EXECUTOR_SERVICE = Executors.newCachedThreadPool();
        TOWNS_TO_CHECK = Collections.unmodifiableList(new LinkedList<String>() {{
            add("Новосибирск");
            add("Омск");
            add("Томск");
            add("Кемерово");
            add("Новокузнецк");
        }});
    }

    public static void main(String[] args) {
        get("/bestDepartments/:what", (request, response) -> {
            try {
                final String what = request.params("what");
                if (what == null) {
                    return createErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "Empty requested string.");
                }

                ExecutorCompletionService<JsonObject> executorCompletionService = new ExecutorCompletionService<>(EXECUTOR_SERVICE);
                for (String where : TOWNS_TO_CHECK) {
                    executorCompletionService.submit(() -> {
                        String searchQuery = String.format(SEARCH_QUERY_TEMPLATE, where, what);
                        JsonObject searchResultJsonObject = executeQuery(searchQuery, SEARCH_URI_PATH);
                        int responseCode = searchResultJsonObject.get("response_code").getAsInt();
                        if (responseCode != HttpStatus.SC_OK) {
                            return null;
                        }
                        JsonArray departments = searchResultJsonObject.get("result").getAsJsonArray();
                        JsonObject department = departments.get(0).getAsJsonObject();
                        String id = department.get("id").getAsString();
                        String hash = department.get("hash").getAsString();
                        String profileQuery = String.format(PROFILE_QUERY_TEMPLATE, id, hash);
                        return executeQuery(profileQuery, PROFILE_URI_PATH);

                    });
                }

                BestDepartmentsResponse bestDepartmentsResponse = new BestDepartmentsResponse();
                for (int i = 0; i < TOWNS_TO_CHECK.size(); i++) {
                    Future<JsonObject> future = executorCompletionService.take();
                    JsonObject jsonObject = future.get();
                    if (jsonObject != null) {
                        DepartmentData departmentData = new DepartmentData();
                        departmentData.setName(jsonObject.get("name").getAsString());
                        String address = jsonObject.get("address").getAsString();
                        String cityName = jsonObject.get("city_name").getAsString();
                        String fullAddress = cityName + ", " + address;
                        departmentData.setFullAddress(fullAddress);
                        JsonElement rating = jsonObject.get("rating");
                        if (rating != null) {

                            departmentData.setRating(rating.getAsString());
                        }
                        bestDepartmentsResponse.addDepartmentData(departmentData);
                    } else {
                        return createErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "Results not found.");
                    }
                }
                bestDepartmentsResponse.setApiVersion(API_VERSION);
                response.status(HttpStatus.SC_OK);
                response.type("application/json; charset=utf-8");
                return bestDepartmentsResponse;
            } catch (ExecutionException | InterruptedException exception) {
                exception.printStackTrace();
                return createErrorResponse(response, HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal server error.");
            }
        }, GSON::toJson);
    }

    private static ErrorResponse createErrorResponse(Response response, int statusCode, String errorMessage) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setApiVersion(API_VERSION);
        errorResponse.setErrorMessage(errorMessage);
        response.type("application/json; charset=utf-8");
        response.status(statusCode);
        return errorResponse;
    }

    private static JsonObject executeQuery(final String query, String uriPath) throws URISyntaxException, IOException {
        HttpGet httpGet = new HttpGet(new URI(URI_SCHEMA, URI_HOST, uriPath, query, null));
        HttpResponse httpResponse = HTTP_CLIENT.execute(httpGet);
        String jsonEntity = EntityUtils.toString(httpResponse.getEntity());
        return GSON.fromJson(jsonEntity, JsonObject.class);
    }
}