package pro.ryzhov.dg.webservice.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import pro.ryzhov.dg.webservice.beans.DepartmentData;

import java.io.IOException;

/**
 * @author Pavel Ryzhov
 */
public class DepartmentDataTypeAdapter extends TypeAdapter<DepartmentData> {

    @Override
    public void write(JsonWriter out, DepartmentData value) throws IOException {
        out.beginObject();

        out.name("name");
        out.value(value.getName());

        out.name("full_address");
        out.value(value.getFullAddress());

        String rating = value.getRating();
        if (rating != null && !rating.equals("0")) {
            out.name("rating");
            out.value(rating);
        }

        out.endObject();
    }

    @Override
    public DepartmentData read(JsonReader in) throws IOException {
        throw new UnsupportedOperationException("Read operation is not supported");
    }
}
