package twork.bollywood.videostatus.object.maincate;

import java.util.List;

/**
 * Created by Jiyan on 5/3/2018.
 */

public class MainCateObject {

    int success;

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public List<MainCateDataObject> getData() {
        return data;
    }

    public void setData(List<MainCateDataObject> data) {
        this.data = data;
    }

    List<MainCateDataObject> data;
}
