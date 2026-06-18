package com.paly.legend.config;

import java.util.ArrayList;
import java.util.List;

public class GuildConfig {

    private List<GuildDonationOptionConfig> donations = new ArrayList<GuildDonationOptionConfig>();
    private List<GuildShopItemConfig> shopItems = new ArrayList<GuildShopItemConfig>();
    private List<GuildActivityConfig> activities = new ArrayList<GuildActivityConfig>();

    public List<GuildDonationOptionConfig> getDonations() {
        return donations;
    }

    public void setDonations(List<GuildDonationOptionConfig> donations) {
        this.donations = donations;
    }

    public List<GuildShopItemConfig> getShopItems() {
        return shopItems;
    }

    public void setShopItems(List<GuildShopItemConfig> shopItems) {
        this.shopItems = shopItems;
    }

    public List<GuildActivityConfig> getActivities() {
        return activities;
    }

    public void setActivities(List<GuildActivityConfig> activities) {
        this.activities = activities;
    }
}
