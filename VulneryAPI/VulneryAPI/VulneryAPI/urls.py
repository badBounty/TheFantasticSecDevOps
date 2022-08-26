from django.contrib import admin
from django.urls import include, path
from django.shortcuts import redirect

from VulnsPoster import views
from . import views as vulneryViews

urlpatterns = [
    path('vulnsPoster/', include('VulnsPoster.urls')), #Path that includes vulnsPoster urls.
    path('dashboards/', include('Dashboards.urls')), #Path that includes Dashboards urls.
    path('admin/', admin.site.urls), #Check later. TODO
    path('home/', vulneryViews.mainPage, name="mainPage"),
    path('', lambda request: redirect(vulneryViews.mainPage, permanent=True)),
    path('login/', vulneryViews.login, name="Login"),
    path('validateLogin/', vulneryViews.validateLogin, name='Validate Login')
]
