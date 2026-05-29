package com.dongmedicine.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<HomeStatistics> statistics;

    public HomeViewModel() {
        statistics = new MutableLiveData<>();
        loadStatistics();
    }

    public LiveData<HomeStatistics> getStatistics() {
        return statistics;
    }

    private void loadStatistics() {
        HomeStatistics stats = new HomeStatistics();
        stats.setPlantCount(156);
        stats.setInheritorCount(23);
        stats.setKnowledgeCount(89);
        stats.setUserCount(1024);
        statistics.setValue(stats);
    }

    public static class HomeStatistics {
        private int plantCount;
        private int inheritorCount;
        private int knowledgeCount;
        private int userCount;

        public int getPlantCount() { return plantCount; }
        public void setPlantCount(int plantCount) { this.plantCount = plantCount; }

        public int getInheritorCount() { return inheritorCount; }
        public void setInheritorCount(int inheritorCount) { this.inheritorCount = inheritorCount; }

        public int getKnowledgeCount() { return knowledgeCount; }
        public void setKnowledgeCount(int knowledgeCount) { this.knowledgeCount = knowledgeCount; }

        public int getUserCount() { return userCount; }
        public void setUserCount(int userCount) { this.userCount = userCount; }
    }
}
