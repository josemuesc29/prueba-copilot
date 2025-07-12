package com.imaginamos.farmatodo.model.algolia;

import java.util.ArrayList;
import java.util.List;

public class RestrictionItem {

	private Long itemId;
	private Long restrictionQuantity;

	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	public Long getRestrictionQuantity() {
		return restrictionQuantity;
	}

	public void setRestrictionQuantity(Long restrictionQuantity) {
		this.restrictionQuantity = restrictionQuantity;
	}

	@Override
	public String toString() {
		return "RestrictionItem{" +
				"itemId=" + itemId +
				", restrictionQuantity=" + restrictionQuantity +
				'}';
	}
}
