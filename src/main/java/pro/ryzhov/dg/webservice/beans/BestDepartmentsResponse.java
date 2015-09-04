package pro.ryzhov.dg.webservice.beans;

import java.util.LinkedList;

/**
 * @author Pavel Ryzhov
 */
public class BestDepartmentsResponse extends Response {

    private LinkedList<DepartmentData> departmentsData = new LinkedList<>();

    public void addDepartmentData(DepartmentData departmentData) {
        String newNodeRating = departmentData.getRating();
        if (newNodeRating == null) {
            departmentsData.addLast(departmentData);
        } else {
            Double rating = Double.valueOf(newNodeRating);
            for (int i = 0; i < departmentsData.size(); i++) {
                String testedNodeRating = departmentsData.get(i).getRating();
                if (testedNodeRating == null || rating >= Double.valueOf(testedNodeRating)) {
                    departmentsData.add(i, departmentData);
                    return;
                }
            }
            departmentsData.addLast(departmentData);
        }
    }
}
