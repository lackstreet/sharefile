package com.company.sharefile.service;

import com.company.sharefile.entity.PlanEntity;
import com.company.sharefile.utils.PlanType;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PlanService {
    public PlanEntity getPlanByType(PlanType planType) {
        PlanEntity planEntity = new PlanEntity();
        return planEntity.findByPlanType(PlanType.BASIC);
    }
}
