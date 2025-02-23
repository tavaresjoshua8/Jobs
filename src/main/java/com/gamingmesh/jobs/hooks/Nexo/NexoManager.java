package com.gamingmesh.jobs.hooks.Nexo;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.listeners.JobsNexoBreakPaymentListener;

public class NexoManager {
    private final Jobs jobs;

    public NexoManager() {
        this.jobs = Jobs.getInstance();
        registerListener();
    }

    public void registerListener() {
        jobs.getServer().getPluginManager().registerEvents(new JobsNexoBreakPaymentListener(), jobs);
    }
}
