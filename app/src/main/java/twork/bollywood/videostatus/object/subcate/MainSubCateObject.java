package twork.bollywood.videostatus.object.subcate;

import java.util.List;

/**
 * Created by Jiyan on 5/3/2018.
 */

public class MainSubCateObject {

    int success, limit, page;
    String total_rec;

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }


    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getTotal_rec() {
        return total_rec;
    }

    public void setTotal_rec(String total_rec) {
        this.total_rec = total_rec;
    }

    public List<MainSubCateDataObject> getData() {
        return data;
    }

    public void setData(List<MainSubCateDataObject> data) {
        this.data = data;
    }

    List<MainSubCateDataObject> data;
}
