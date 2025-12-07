import { Routes } from '@angular/router';
import {authGuard} from "./core/guards/auth-guard";

export const routes: Routes = [
    {
        path: '',
        redirectTo: '/dashboard',
        pathMatch: 'full',
    },
    {
        path: 'auth',
        loadChildren: () => import('./features/auth/auth-module').then(load => load.AuthModule)
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
