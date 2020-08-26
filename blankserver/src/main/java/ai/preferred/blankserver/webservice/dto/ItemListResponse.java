package ai.preferred.blankserver.webservice.dto;

import ai.preferred.blankserver.webservice.models.Items;

import java.util.List;

/**
 * @author hpminh@apcs.vn
 */
public class ItemListResponse {
    List<Items> items;

    public ItemListResponse(List<Items> items) {
        this.items = items;
    }

    public List<Items> getItems() {
        return items;
    }

    public void setItems(List<Items> items) {
        this.items = items;
    }
}
