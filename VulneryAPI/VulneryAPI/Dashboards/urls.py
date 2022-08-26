from django.shortcuts import redirect
from django.urls import path

from . import views
from VulneryAPI import views as vulneryViews

urlpatterns = [
    path('', lambda request: redirect(vulneryViews.mainPage, permanent=True)), #Path to redirect to default page
    path('dashboard/', views.dashboard, name="Dashboard")
]
