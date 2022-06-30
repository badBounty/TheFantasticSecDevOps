from django.contrib import admin
from django.urls import include, path

from VulnsPoster import views
from . import views as orchViews

urlpatterns = [
    path('vulnsPoster/', include('VulnsPoster.urls')), #Path that includes vulnsPoster urls.
    path('admin/', admin.site.urls), #Check later
    path('', orchViews.defaultPage, name="Default Page")
]
