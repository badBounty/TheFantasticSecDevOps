from django.shortcuts import render, redirect
from django.http import HttpResponse, JsonResponse, HttpResponseRedirect

#View default page.

def defaultPage(request):
    return render(request, "defaultPage.html")