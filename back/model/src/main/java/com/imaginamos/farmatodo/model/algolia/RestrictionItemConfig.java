package com.imaginamos.farmatodo.model.algolia;

import java.util.ArrayList;
import java.util.List;

public class RestrictionItemConfig {
	private List<RestrictionItem> restrictionItems = new ArrayList<>();

	public List<RestrictionItem> getRestrictionItems() {
		return restrictionItems;
	}

	public void setRestrictionItems(List<RestrictionItem> restrictionItems) {
		this.restrictionItems = restrictionItems;
	}

	@Override
	public String toString() {
		return "RestrictionItemConfig{" +
				"restrictionItemList=" + restrictionItems +
				'}';
	}
}
