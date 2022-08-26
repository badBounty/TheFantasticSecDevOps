from django.shortcuts import redirect
from django.urls import path

from . import views
from VulneryAPI import views as vulneryViews

urlpatterns = [
    path('', lambda request: redirect(vulneryViews.mainPage, permanent=True)), #Path to redirect to default page
    path('vulns/', views.vulns, name='Vulns'), #Path to list vulns
    path('postVulnSAST/', views.postVulnSAST, name='Post SAST Vulns'), #Path to post SAST vulns
    path('postVulnDAST/', views.postVulnDAST, name='Post DAST Vulns'), #Path to post SAST vulns
    path('postVulnInfra/', views.postVulnInfra, name='Post Infra Vulns'),
    path('vulnsType/', views.vulnsType, name='Get Vuln Types'),
    path('severities/', views.severities, name='Get Severities'),
    path('status/', views.status, name='Get Status'),
    path('vulnsSAST/', views.vulnsSAST, name='Get SAST Vulns'),
    path('vulnsDAST/', views.vulnsDAST, name='Get DAST Vulns'),
    path('vulnsInfra/', views.vulnsInfra, name='Get Infra Vulns'),
    path('deleteVulnSAST/', views.deleteVulnSAST, name='Delete SAST Vuln'),
    path('deleteVulnDAST/', views.deleteVulnDAST, name='Delete DAST Vuln'),
    path('deleteVulnInfra/', views.deleteVulnInfra, name='Delete Infra Vuln'),
    path('updateVulnDAST/', views.updateVulnDAST, name='Update DAST Vuln'),
    path('updateVulnSAST/', views.updateVulnSAST, name='Update SAST Vuln'),
    path('updateVulnInfra/', views.updateVulnInfra, name='Update Infra Vuln')
]