import { Routes } from '@angular/router';
import {authGuard} from "./core/guards/auth-guard";
import {UploadComponent} from "./features/upload/upload";

export const routes: Routes = [
    {
        path: '',
        redirectTo: '/upload',
        pathMatch: 'full'
    },
    {
        path: 'upload',
        component: UploadComponent,
        canActivate: [authGuard]
    },
    {
        path: 'dashboard',
        loadChildren: () => import('./features/dashboard/dashboard-module').then(load => load.DashboardModule),
        canActivate: [authGuard]
    },
    {
        path: 'transfer',
        loadChildren: () => import('./features/transfer/transfer-module').then(load => load.TransferModule)
    },
    {
        path: '**',
        redirectTo: '/dashboard'
    }
];
