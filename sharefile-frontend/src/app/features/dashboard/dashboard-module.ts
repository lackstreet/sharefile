import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { DashboardRoutingModule } from './dashboard-routing-module';
import {Home} from "./home/home";


@NgModule({
  imports: [
    CommonModule,
    DashboardRoutingModule
  ]
})
export class DashboardModule { }
