import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {Download} from "./download/download";
import {Create} from "./create/create";

const routes: Routes = [
  { path: 'new', component: Create },
  { path: 'download/:token', component: Download }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TransferRoutingModule { }
