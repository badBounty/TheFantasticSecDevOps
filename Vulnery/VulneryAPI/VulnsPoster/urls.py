from django.shortcuts import redirect
from django.urls import path

from . import views
from VulneryAPI import views as vulneryViews

urlpatterns = [
    path('', lambda request: redirect(vulneryViews.defaultPage, permanent=True)), #Path to redirect to default page
    path('vulns/', views.vulns, name='Vulns'), #Path to list vulns
    path('vulnsErrors/', views.vulnsErrors, name='Vulns Errors'), #Path to vuln errors
    path('postVulns/', views.postVulns, name='Post Vulns'), #Path to post vulns
]