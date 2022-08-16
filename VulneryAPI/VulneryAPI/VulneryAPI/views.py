from django.shortcuts import render, redirect
from django.http import HttpResponse, JsonResponse, HttpResponseRedirect

#View default page.

def mainPage(request):
    return render(request, "mainPage.html")