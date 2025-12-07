import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { TransferRoutingModule } from './transfer-routing-module';
import {Create} from "./create/create";
import {Download} from "./download/download";


@NgModule({
  imports: [
    CommonModule,
    TransferRoutingModule
  ]
})
export class TransferModule { }
