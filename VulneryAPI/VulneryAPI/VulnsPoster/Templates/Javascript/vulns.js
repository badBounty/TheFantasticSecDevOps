$(document).ready(function(){ 
    getVulnTypes("#dropdownVulnTypeMenu", "#ddVulnType", "vulnType");
    getSeverities('#dropdownSeverityMenu', '#ddSeverity', 'severity', false); 
    getStatus('#dropdownStatusMenu', '#ddStatus', 'status', false);
    $("#txtDateTo").data("DateTimePicker").date(moment());
    $("#txtDateFrom").data("DateTimePicker").date(moment());
    setToastrOptions();
});

$('#btnVulnSearch').on('click',function(e){
    searchVulns();
    e.preventDefault();
});

function setDataToSearchVulns(){
    try{
        let selectedVulnType = ($('#ddVulnType').val()).trim();
        let selectedSeverity = ($('#ddSeverity').val()).trim();
        let selectedStatus = ($('#ddStatus').val()).trim();
        let getAllVulns = $('#flexCheckDates').prop('checked');
        let fromDate = formatDate($("#txtDateFrom").val());
        let toDate = formatDate($("#txtDateTo").val()); 
        let data = {
            "vulnType": selectedVulnType, 
            "severity": selectedSeverity,
            "status":selectedStatus,
            "getAllDates": getAllVulns, 
            "fromDate": fromDate, 
            "toDate": toDate
        };
        return data;
    }
    catch(error){
        setToastrError();
    }
}

//Search vulnerabilities 
function searchVulns(){
    try{
        let dataToSend = setDataToSearchVulns();
        $('.newBtn :button').remove();
        $('#cardVulns').addClass('d-none');
        setLoader('#containerVulns');
        fetch(`/vulnsPoster/vulns${dataToSend['vulnType']}/`,{
            method: 'POST',
            body: JSON.stringify(dataToSend),
            headers: {
                'Content-Type': 'application/json'
            },
            cache: "no-cache"
        }).then(function(response){
            setLoaderHide("#containerVulns");
            if(response.ok){
                return response.json().then(function(dataJSON){      
                    $('#sidebar').addClass('active'); 
                    resetDataTables();    
                    if(dataToSend['vulnType'] == "SAST"){ //Check if type is SAST or DAST Dynamically. TODO
                        loadVulnsSAST(dataJSON); 
                    }
                    else if (dataToSend['vulnType'] == "DAST"){
                        loadVulnsDAST(dataJSON);
                    }     
                    else if(dataToSend['vulnType'] == "Infra"){
                        loadVulnsInfra(dataJSON);
                    }    
                    $('#cardVulns').removeClass('d-none');                   
                });
            }
            else{              
                setToastrError();
            }
        });
    }
    catch(error){
        setToastrError();
    }
}

//----------------SAST-----------------//

//DataTable SAST vulns

function loadVulnsSAST(dataJSON){
    try{
        //if(!resetDataTable('#dtVulnsSAST')){
            //appendCreateButtonToCard('btnNewVulnSAST','Create','.newBtn');
            appendDataTable('dtVulnsSAST');
            let dtVulnsSAST =  $('#dtVulnsSAST').DataTable({ 
                "filter": true, 
                "autoWidth": true,
                "columnDefs": [
                    {
                        "targets": "_all",
                        "defaultContent": "-",
                        "className": "dt-center",
                    },
                    {
                        "targets": 0,
                        "visible": false
                    }
                ],
                "columns": [
                    { "data": "vulnID"},
                    { "data": "Title", title: "Title"},
                    { "data": "Component", title: "Component"},
                    { "data": "Pipeline", title: "Pipeline"},
                    { "data": "Date", title: "Date"},
                    { "data": "Severity", title: "Severity"},
                    { "data": "Status", title: "Status"},
                    {  title: "View",
                            render: function(){
                                return "<button class='btn shadow-none' id='btnViewVuln'><i class='fa-solid fa-eye'></i></button>"
                            }
                    },
                    {  title: "Edit",
                            render: function(){
                                return "<button class='btn shadow-none' id='btnEditVuln'><i class='fa-solid fa-pen-to-square'></i></button>"
                            }
                    },
                    {  title: "Delete",
                            render: function(){
                                return "<button class='btn shadow-none' id='btnDeleteVuln'><i class='fa-solid fa-trash-can'></i></button>"
                            }
                    }
                ]
            });
        
            $(dataJSON).each(function() { 
                dtVulnsSAST.row.add({
                    "vulnID": this.vulnID,
                    "Title": this.Title,
                    "Component": this.Component,
                    "Pipeline": this.Pipeline,
                    "Date": this.Date,
                    "Severity": this.Severity,
                    "Status": this.Status
                }).draw();
                dtVulnsSAST.columns.adjust();
            });

            setToastrSuccess();

            $('#dtVulnsSAST').on('click', '#btnViewVuln', function (e){
                let rowData =  dtVulnsSAST.row($(this).parents('tr')).data();
                loadVulnSASTData(rowData, false)
                e.preventDefault();
            });

            $('#dtVulnsSAST').on('click', '#btnEditVuln', function (e){
                let rowData =  dtVulnsSAST.row($(this).parents('tr')).data();
                loadVulnSASTData(rowData, true)
                e.preventDefault();
            });

            $('#dtVulnsSAST').on('click', '#btnDeleteVuln', function (e){
                let rowData =  dtVulnsSAST.row($(this).parents('tr')).data();
                deleteVuln(rowData, 'deleteVulnSAST');
                e.preventDefault();
            });
        //} 
    }
    catch(error){
        setToastrError();
    }
}

//Loads a single SAST vuln by selected DataTable row

function loadVulnSASTData(rowData, isEdit){
    try{
        if(rowData){
            let rowDataKeys = Object.keys(rowData);
            fetch(`/vulnsPoster/vulnsSAST/?${rowDataKeys[0]}=${rowData[rowDataKeys[0]]}`,{
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            })
            .then(function(response){
                if(response.ok){
                    return response.json().then(function(dataJSON){
                        setModalSASTData(dataJSON, isEdit, 'btnVulnSaveSAST', 'Edit');
                    })
                }
                else{
                    setToastrError();
                }
            });
        } 
    }
    catch(error){
        setToastrError();
    }
}

$(document).on('click','#btnNewVulnSAST', function(){
    clearControls('#modalViewVulnSAST');
    setModalSASTData(null, true, 'btnVulnCreateSAST', 'Create');
});

//Sets SAST Modal

function setModalSASTData(SASTVuln, isEdit, buttonId, actionVuln){
    clearDropdown('#ddSeveritySASTModalMenu');
    clearDropdown('#ddStatusSASTModalMenu');
    getSeverities('#ddSeveritySASTModalMenu', '#ddSeveritySASTModal', 'severity', true);
    getStatus('#ddStatusSASTModalMenu', '#ddStatusSASTModal', 'status', true);
    setDateTimePicker('#vulnDateSAST');
    removeBtnFromModal('#modalViewVulnSAST');
    if(SASTVuln){
        loadModalSASTData(SASTVuln);
    }
    if(isEdit){
        setModalEditOrCreate('#ddSeveritySASTModal','#ddStatusSASTModal','#modalViewVulnSAST',buttonId, actionVuln);
    }
    else{
        setModalView('#ddSeveritySASTModal','#ddStatusSASTModal','#modalViewVulnSAST');
    }
    $('#modalViewVulnSAST').modal('show');
}

function loadModalSASTData(SASTVuln){
    try{
        $('#vulnIDSAST').val(SASTVuln['vulnID']); //ENCRYPT ID
        $('#vulnTitleSAST').val(SASTVuln['Title']);
        $('#vulnDescriptionSAST').val(SASTVuln['Description']);
        $('#vulnComponentSAST').val(SASTVuln['Component']);
        $('#vulnLineSAST').val(SASTVuln['Line']);
        $('#vulnAffectedCodeSAST').val(SASTVuln['AffectedCode']);
        $('#vulnCommitSAST').val(SASTVuln['Commit']);
        $('#vulnUsernameSAST').val(SASTVuln['Username']);
        $('#vulnPipelineSAST').val(SASTVuln['Pipeline']);
        $('#vulnBranchSAST').val(SASTVuln['Branch']);
        $('#vulnLanguageSAST').val(SASTVuln['Language']);
        $('#ddSeveritySASTModal').val(SASTVuln['Severity']);
        $('#ddSeveritySASTModal').text(SASTVuln['Severity']);
        $('#vulnDateSAST').val(SASTVuln['Date']);
        $('#ddStatusSASTModal').val(SASTVuln['Status']);
        $('#ddStatusSASTModal').text(SASTVuln['Status']);
        $('#vulnRecommendationSAST').val(SASTVuln['Recommendation']);
    }
    catch(error){
        setToastrError();
    }
}

//Updates SAST vuln

$(document).on('click','#btnVulnSaveSAST', function(e){
    updateOrCreateSASTVuln('update');
    e.preventDefault();
});

$(document).on('click','#btnVulnCreateSAST', function(e){
    updateOrCreateSASTVuln('post');
    e.preventDefault();
});

function updateOrCreateSASTVuln(action){
    let jsonSASTVuln = getJSONUpdateSASTVuln();
    setLoader('#containerVulns');
    fetch(`/vulnsPoster/${action}VulnSAST/`,{
        method: 'POST',
        body: JSON.stringify(jsonSASTVuln),
        headers: {
            'Content-Type': 'application/json'
        }
    }).then(function(response){
        if(response.ok){
            sleep(1000).then(()=>{
                setLoaderHide('#containerVulns');
                setToastrSuccess();
                $('#modalViewVulnSAST').modal('hide');
                return searchVulns();
            }); 
        }
        else{
            setToastrError();
        }
    });
}

function getJSONUpdateSASTVuln(){
    let jsonSASTVuln = {
        'vulnID': $('#vulnIDSAST').val(), //ENCRYPT ID
        'Title': $('#vulnTitleSAST').val(),
        'Description': $('#vulnDescriptionSAST').val(),
        'Component': $('#vulnComponentSAST').val(),
        'Line': $('#vulnLineSAST').val(),
        'Affected_code': $('#vulnAffectedCodeSAST').val(),
        'Commit': $('#vulnCommitSAST').val(),
        'Username': $('#vulnUsernameSAST').val(),
        'Pipeline_name': $('#vulnPipelineSAST').val(),
        'Branch': $('#vulnBranchSAST').val(),
        'Language': $('#vulnLanguageSAST').val(),
        'Date': formatDate($('#vulnDateSAST').val()), //Format date
        'Recommendation': $('#vulnRecommendationSAST').val(),
        'Severity': $('#ddSeveritySASTModal').val(), //Get selected option
        'Status': $('#ddStatusSASTModal').val(), //Get selected option
        'Hash': "" //Calculate Hash
    }
    return jsonSASTVuln;
}

//----------------DAST-----------------//

//DataTable DAST vulns

function loadVulnsDAST(dataJSON){
    try{
        if(!resetDataTable('#dtVulnsDAST')){
            //appendCreateButtonToCard('btnNewVulnDAST','Create','.newBtn');
            appendDataTable('dtVulnsDAST');
            let dtVulnsDAST =  $('#dtVulnsDAST').DataTable({ 
                "filter": true, 
                "autoWidth": true,
                "columnDefs": [
                    {
                        "targets": "_all",
                        "className": "dt-center",
                    },
                    {
                        "targets": 0,
                        "visible": false
                    }
                ],
                "columns": [        
                    { "data": "vulnID"},
                    { "data": "Title", title: "Title"},
                    { "data": "AffectedResource", title: "Affected Resources"},
                    { "data": "AffectedUrls", title: "Affected URLs"},
                    { "data": "Date", title: "Date"},
                    { "data": "Severity", title: "Severity"},
                    { "data": "Status", title: "Status"},
                    {  title: "View",
                            render: function(){
                                return "<button class='btn shadow-none' id='btnViewVuln'><i class='fa-solid fa-eye'></i></button>"
                            }
                    },
                    {  title: "Edit",
                            render: function(){
                                return "<button class='btn shadow-none' id='btnEditVuln'><i class='fa-solid fa-pen-to-square'></i></button>"
                            }
                    },
                    {  title: "Delete",
                            render: function(){
                                return "<button class='btn shadow-none' id='btnDeleteVuln'><i class='fa-solid fa-trash-can'></i></button>"
                            }
                    }
                ]                      
            });
        
            $(dataJSON).each(function() { 
                dtVulnsDAST.row.add({
                    "vulnID": this.vulnID,
                    "Title": this.Title,
                    "AffectedResource": this.AffectedResource,
                    "AffectedUrls": this.AffectedURLs,
                    "Date": this.Date,
                    "Severity": this.Severity,
                    "Status": this.Status
                }).draw();
                dtVulnsDAST.columns.adjust();
            });

            setToastrSuccess();
            
            $('#dtVulnsDAST').on('click', '#btnViewVuln', function (e){
                let rowData =  dtVulnsDAST.row($(this).parents('tr')).data();
                loadVulnDASTData(rowData, false)
                e.preventDefault();
            });

            $('#dtVulnsDAST').on('click', '#btnEditVuln', function (e){
                let rowData =  dtVulnsDAST.row($(this).parents('tr')).data();
                loadVulnDASTData(rowData, true)
                e.preventDefault();
            });

            $('#dtVulnsDAST').on('click', '#btnDeleteVuln', function (e){
                let rowData =  dtVulnsDAST.row($(this).parents('tr')).data();
                deleteVuln(rowData, 'deleteVulnDAST');
                e.preventDefault();
            });
            
        }   
    }
    catch(error){
        setToastrError();
    }
}

//Loads a single DAST vuln by selected DataTable row

function loadVulnDASTData(rowData, isEdit){
    try{
        if(rowData){
            let rowDataKeys = Object.keys(rowData);
            fetch(`/vulnsPoster/vulnsDAST/?${rowDataKeys[0]}=${rowData[rowDataKeys[0]]}`,{
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            })
            .then(function(response){
                return response.json().then(function(dataJSON){
                    setModalDASTData(dataJSON, isEdit, 'btnVulnSaveDAST', 'Edit');
                })
            });
        }
    }
    catch(error){
        setToastrError();
    }
}

$(document).on('click','#btnNewVulnDAST', function(){
    clearControls('#modalViewVulnDAST');
    setModalDASTData(null, true, 'btnVulnCreateDAST', 'Create');
});

//Sets DAST Modal

function setModalDASTData(DASTVuln, isEdit, buttonId, action){
    setDateTimePicker('#vulnDateDAST');
    clearDropdown('#ddSeverityDASTModalMenu');
    clearDropdown('#ddStatusDASTModalMenu');
    getSeverities('#ddSeverityDASTModalMenu', '#ddSeverityDASTModal', 'severity', true);
    getStatus('#ddStatusDASTModalMenu', '#ddStatusDASTModal', 'status', true);
    removeBtnFromModal('#modalViewVulnDAST');
    if(DASTVuln){
        loadModalDASTData(DASTVuln);
    }
    if(isEdit){
        setModalEditOrCreate('#ddSeverityDASTModal','#ddStatusDASTModal','#modalViewVulnDAST',buttonId,action);
    }
    else{
        setModalView('#ddSeverityDASTModal','#ddStatusDASTModal','#modalViewVulnDAST');
    }
    $('#modalViewVulnDAST').modal('show');
}

function loadModalDASTData(DASTVuln){
    try{
        $('#vulnIDDAST').val(DASTVuln['vulnID']); //ENCRYPT ID
        $('#vulnTitleDAST').val(DASTVuln['Title']);
        $('#vulnDescriptionDAST').val(DASTVuln['Description']);
        $('#vulnAffectedResourceDAST').val(DASTVuln['AffectedResource']);
        $('#vulnAffectedURLsDAST').val(DASTVuln['AffectedURLs']);  
        $('#vulnDateDAST').val(DASTVuln['Date']); 
        $('#vulnRecommendationDAST').val(DASTVuln['Recommendation']);
        $('#ddSeverityDASTModal').val(DASTVuln['Severity']); //Fix
        $('#ddSeverityDASTModal').text(DASTVuln['Severity']); //Fix
        $('#ddStatusDASTModal').val(DASTVuln['Status']); //Fix
        $('#ddStatusDASTModal').text(DASTVuln['Status']); //Fix
    }
    catch(error){
        setToastrError();
    }
}

//Updates DAST vuln

$(document).on('click','#btnVulnSaveDAST', function(e){
    updateOrCreateDASTVuln('update');
    e.preventDefault();
});

$(document).on('click','#btnVulnCreateDAST', function(e){
    updateOrCreateDASTVuln('post');
    e.preventDefault();
});

function updateOrCreateDASTVuln(action){
    let jsonDASTVuln = getJSONUpdateDASTVuln();
    setLoader('#containerVulns');
    fetch(`/vulnsPoster/${action}VulnDAST/`,{
        method: 'POST',
        body: JSON.stringify(jsonDASTVuln),
        headers: {
            'Content-Type': 'application/json'
        },
        cache: "reload"
    }).then(function(response){
        if(response.ok){
            sleep(500).then(()=>{
                setLoaderHide('#containerVulns');
                setToastrSuccess();
                $('#modalViewVulnDAST').modal('hide');
                return searchVulns();
            }); 
        }
        else{
            setToastrError();
        }
    });
    
}

function getJSONUpdateDASTVuln(){
    let jsonDASTVuln = {
        'vulnID': $('#vulnIDDAST').val(),
        'Title': $('#vulnTitleDAST').val(),
        'Description': $('#vulnDescriptionDAST').val(),
        'Affected_resource': $('#vulnAffectedResourceDAST').val(),
        'Affected_urls': $('#vulnAffectedURLsDAST').val(),
        'Date': formatDate($('#vulnDateDAST').val()), //Format date
        'Recommendation': $('#vulnRecommendationDAST').val(),
        'Severity': $('#ddSeverityDASTModal').val(), //Get selected option
        'Status': $('#ddStatusDASTModal').val() //Get selected option
    }
    return jsonDASTVuln;
}

//----------------Infra-----------------//

//DataTable Infra vulns

function loadVulnsInfra(dataJSON){
    try{
        if(!resetDataTable('#dtVulnsInfra')){
            //appendCreateButtonToCard('btnNewVulnInfra','Create','.newBtn');
            appendDataTable('dtVulnsInfra');
            let dtVulnsInfra =  $('#dtVulnsInfra').DataTable({ 
                "filter": true, 
                "autoWidth": true,
                "columnDefs": [
                    {
                        "targets": "_all",
                        "defaultContent": "-",
                        "className": "dt-center",
                    },
                    {
                        "targets": 0,
                        "visible": false
                    }
                ],
                "columns": [
                    { "data": "vulnID"},
                    { "data": "Title", title: "Title"},
                    { "data": "Description", title: "Description"},
                    { "data": "Domain", title: "Domain"},
                    { "data": "Date", title: "Date"},
                    { "data": "Severity", title: "Severity"},
                    { "data": "Status", title: "Status"},
                    {  title: "View",
                            render: function(){
                                return "<button class='btn shadow-none' id='btnViewVuln'><i class='fa-solid fa-eye'></i></button>"
                            }
                    },
                    {  title: "Edit",
                            render: function(){
                                return "<button class='btn shadow-none' id='btnEditVuln'><i class='fa-solid fa-pen-to-square'></i></button>"
                            }
                    },
                    {  title: "Delete",
                            render: function(){
                                return "<button class='btn shadow-none' id='btnDeleteVuln'><i class='fa-solid fa-trash-can'></i></button>"
                            }
                    }
                ]
            });
        
            $(dataJSON).each(function() { 
                dtVulnsInfra.row.add({
                    "vulnID": this.vulnID,
                    "Title": this.Title,
                    "Description": this.Description,
                    "Domain": this.Domain,
                    "Date": this.Date,
                    "Severity": this.Severity,
                    "Status": this.Status
                }).draw();
                dtVulnsInfra.columns.adjust();
            });

            setToastrSuccess();

            $('#dtVulnsInfra').on('click', '#btnViewVuln', function (e){
                let rowData =  dtVulnsInfra.row($(this).parents('tr')).data();
                loadVulnInfraData(rowData, false)
                e.preventDefault();
            });

            $('#dtVulnsInfra').on('click', '#btnEditVuln', function (e){
                let rowData =  dtVulnsInfra.row($(this).parents('tr')).data();
                loadVulnInfraData(rowData, true)
                e.preventDefault();
            });

            $('#dtVulnsInfra').on('click', '#btnDeleteVuln', function (e){
                let rowData =  dtVulnsInfra.row($(this).parents('tr')).data();
                deleteVuln(rowData, 'deleteVulnInfra');
                e.preventDefault();
            });
        } 
    }
    catch(error){
        setToastrError();
    }
}

$(document).on('click','#btnNewVulnInfra', function(){
    clearControls('#modalViewVulnInfra');
    setModalInfraData(null, true, 'btnVulnCreateInfra', 'Create', 'btnCVSFileInfra');
});

//Loads a single Infra vuln by selected DataTable row

function loadVulnInfraData(rowData, isEdit){
    try{
        if(rowData){
            let rowDataKeys = Object.keys(rowData);
            fetch(`/vulnsPoster/vulnsInfra/?${rowDataKeys[0]}=${rowData[rowDataKeys[0]]}`,{
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            })
            .then(function(response){
                if(response.ok){
                    return response.json().then(function(dataJSON){
                        setModalInfraData(dataJSON, isEdit, 'btnVulnSaveInfra', 'Edit', null);
                    })
                }
                else{
                    setToastrError();
                }
            });
        } 
    }
    catch(error){
        setToastrError();
    }
}

//Sets Infra Modal

function setModalInfraData(InfraVuln, isEdit, buttonId, action, buttonCSV){
    clearDropdown('#ddSeverityInfraModalMenu');
    clearDropdown('#ddStatusInfraModalMenu');
    getSeverities('#ddSeverityInfraModalMenu', '#ddSeverityInfraModal', 'severity', true);
    getStatus('#ddStatusInfraModalMenu', '#ddStatusInfraModal', 'status', true);
    setDateTimePicker('#vulnDateInfra');
    removeBtnFromModal('#modalViewVulnInfra');
    if(InfraVuln){
        loadModalInfraData(InfraVuln);
    }
    if(isEdit){
        $('#modalViewVulnInfra').find('.modal-footer').removeClass('justify-content-between');
        setModalEditOrCreate('#ddSeverityInfraModal','#ddStatusInfraModal','#modalViewVulnInfra',buttonId, action);
        if(buttonCSV){
            $('#modalViewVulnInfra').find('.modal-footer').addClass('justify-content-between');
            let btnUploadCSV = `<button type='button' class='btn btn-dark btn-sm' id=${buttonCSV}>Upload CSV</button>`;
            $('#modalViewVulnInfra').find('.modal-footer').append(btnUploadCSV);
            $('<input>').attr({
                type: 'file',
                name: buttonCSV,
                class: 'form-control form-control-sm w-75',
                accept: ".csv"
            }).appendTo($('#modalViewVulnInfra').find('.modal-footer'));
            
        } 
        let childElements = $('#modalViewVulnInfra').find('.modal-footer').children();
        $('#modalViewVulnInfra').find('.modal-footer').append(childElements.get().reverse());
    }
    else{
        setModalView('#ddSeverityInfraModal','#ddStatusInfraModal','#modalViewVulnInfra');
    }
    $('#modalViewVulnInfra').modal('show');
}

function loadModalInfraData(InfraVuln){
    try{
        $('#vulnIDInfra').val(InfraVuln['vulnID']); //ENCRYPT ID
        $('#vulnTitleInfra').val(InfraVuln['Title']);
        $('#vulnDescriptionInfra').val(InfraVuln['Description']);
        $('#vulnObservationInfra').val(InfraVuln['Observation']);
        $('#vulnDomainInfra').val(InfraVuln['Domain']);
        $('#vulnSubdomainInfra').val(InfraVuln['Subdomain']);
        $('#vulnExtraInfoInfra').val(InfraVuln['ExtraInfo']);
        $('#vulnCVSSScoreInfra').val(InfraVuln['CVSS_Score']);
        $('#vulnLanguageInfra').val(InfraVuln['Language']);
        $('#ddSeverityInfraModal').val(InfraVuln['Severity']);
        $('#ddSeverityInfraModal').text(InfraVuln['Severity']);
        $('#vulnDateInfra').val(InfraVuln['Date']);
        $('#ddStatusInfraModal').val(InfraVuln['Status']);
        $('#ddStatusInfraModal').text(InfraVuln['Status']);
        $('#vulnRecommendationInfra').val(InfraVuln['Recommendation']);
    }
    catch(error){
        setToastrError();
    }
}

//Updates or creates Infra vuln

$(document).on('click','#btnVulnSaveInfra', function(e){
    updateOrCreateInfraVuln('update');
    e.preventDefault();
});

$(document).on('click','#btnVulnCreateInfra', function(e){
    updateOrCreateInfraVuln('post');
    e.preventDefault();
});


function updateOrCreateInfraVuln(action){
    let jsonInfraVuln = getJSONUpdateInfraVuln();
    setLoader('#containerVulns');
    fetch(`/vulnsPoster/${action}VulnInfra/`,{
        method: 'POST',
        body: JSON.stringify(jsonInfraVuln),
        headers: {
            'Content-Type': 'application/json'
        }
    }).then(function(response){
        if(response.ok){
            sleep(500).then(()=>{
                setLoaderHide('#containerVulns');
                setToastrSuccess();
                $('#modalViewVulnInfra').modal('hide');
                return searchVulns();
            }); 
        }
        else{
            setToastrError();
        }
    });
}

function getJSONUpdateInfraVuln(){
    let jsonInfraVuln = {
        'vulnID': $('#vulnIDInfra').val(),
        'Title': $('#vulnTitleInfra').val(),
        'Description': $('#vulnDescriptionInfra').val(),
        'Observation': $('#vulnObservationInfra').val(),
        'Domain': $('#vulnDomainInfra').val(),
        'Subdomain': $('#vulnSubdomainInfra').val(),
        'Extra_info': $('#vulnExtraInfoInfra').val(),
        'CVSS_Score': $('#vulnCVSSScoreInfra').val(),
        'Language': $('#vulnLanguageInfra').val(),
        'Date': formatDate($('#vulnDateInfra').val()), //Format date
        'Recommendation': $('#vulnRecommendationInfra').val(),
        'Severity': $('#ddSeverityInfraModal').val(), //Get selected option
        'Status': $('#ddStatusInfraModal').val() //Get selected option
    }
    return jsonInfraVuln;
}

//Load Infra vuln by CSV

$(document).on('click','#btnCVSFileInfra', function(e){
    uploadInfraVulnCSV(e);
    e.preventDefault();
});

function uploadInfraVulnCSV(e){
    let recievedFile = $("input[name='btnCVSFileInfra']").get(0).files[0];
    if(recievedFile != undefined){
        alert("ok");
        let convertedFile = convertFileToBase64(recievedFile);
        //alert(convertedFile);
        e.preventDefault();
    }
    //Get file, convert to base64 and upload to server.
}

function convertFileToBase64(file){
    const reader = new FileReader();
    reader.readAsDataURL(file);
    let base64;
    reader.onload = function(file) {
        base64 = reader.result.replace('data:', '').replace(/^.+,/, '');
    }
    return base64;
}

//----------------Generic-----------------//

//Deletes vuln

function deleteVuln(rowData, endpoint){
    try{
        if(rowData){
            SwalDelete(rowData.Title).then((result) => {
                if (result.isConfirmed) {
                    let rowDataKeys = Object.keys(rowData);
                    let dataToSend = {
                        "vulnID": rowData[rowDataKeys[0]]
                    }
                    fetch(`/vulnsPoster/${endpoint}/`,{
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify(dataToSend)
                    })
                    .then(function(response){
                        if(response.ok){
                            setLoader('#containerVulns');
                            sleep(500).then(()=>{
                                setLoaderHide('#containerVulns');
                                setToastrSuccess();
                                return searchVulns();
                            }); 
                        }
                        else{
                            setToastrError();
                        }
                    });
                }
            })
        }
    }
    catch(error){
        setToastrError();
    }
}

function setModalView(ddSeverity, ddStatus, modal){
    $(ddSeverity).addClass('disabled');
    $(ddStatus).addClass('disabled');
    $(modal).find('input').prop("readonly",true);
    $(modal).find('textarea').prop("readonly",true);
    $(modal).find('h5').text('View vulnerability');
    $(modal).find('.modal-footer').children().remove();
}

function setModalEditOrCreate(ddSeverity, ddStatus, modal, buttonId, actionVuln){
    setModalPropsEnabled(ddSeverity, ddStatus, modal);
    $(modal).find('h5').text(`${actionVuln} vulnerability`);
    addSaveButtonToModal(modal, buttonId, actionVuln);
}

function setModalPropsEnabled(ddsev, ddstat, modal){
    $(ddsev).removeClass('disabled');
    $(ddstat).removeClass('disabled');
    $(modal).find('input').prop("readonly",false);
    $(modal).find('textarea').prop("readonly",false);
}

function addSaveButtonToModal(modal, buttonId, actionVuln){
    if($(modal).find('.modal-footer').children().length==0){
        let saveButton = `<button type="button" class="btn btn-dark btn-sm" id=${buttonId}>${actionVuln} vuln</button>`
        $(modal).find('.modal-footer').append(saveButton);
    } 
}

function appendCreateButtonToCard(buttonId, action, card){
    $(card).append(`<button type="button" class="btn btn-dark shadow" id=${buttonId}>${action} vuln</button>`);
}

function removeBtnFromModal(modal){
    $(modal).find('.modal-footer').children().remove();
}

function SwalDelete(vulnName){
    return Swal.fire({
        title: 'Confirm the delete?',
        text: `Vuln: ${vulnName}`,
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#212529',
        cancelButtonColor: '#828282',
        confirmButtonText: 'Yes'
    })
}

//Resets DataTables 

function resetDataTables(){
    $('.table').each(function(){
        $(`#${this.id}`).DataTable().clear();
        $(`#${this.id}`).DataTable().destroy();
        $(`#${this.id} tbody`).empty();
        $(`#${this.id} thead`).empty();
        $(`#${this.id}`).remove();
    });
}

//Reset a DataTable

function resetDataTable(dtName){
    if ($.fn.dataTable.isDataTable(dtName)) {
        $(dtName).DataTable().clear();
        $(dtName).DataTable().destroy();
        $(dtName+' tbody').empty();
        $(dtName+' thead').empty();
        $(dtName).remove();
        return true
    }
    return false
}

//Gets Vuln Types

function getVulnTypes(ddMenu, dd, field){
    try{
        fetch('/vulnsPoster/vulnsType',{
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then(function(response){
            return response.json().then(function(dataJSON){
                loadAttribute(dataJSON, ddMenu, dd, field, false);
            });
        });
    }
    catch(error){
        setToastrError();
    }
}

//Gets Severities

function getSeverities(ddMenu, dd, field, removeAll){
    try{
        fetch('/vulnsPoster/severities/',{
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then(function(response){
            return response.json().then(function(dataJSON){
                loadAttribute(dataJSON, ddMenu, dd, field, removeAll);
            });
        });
    }
    catch(error){
        setToastrError();
    }
}

//Gets Status

function getStatus(ddMenu, dd, field, removeAll){
    try{
        fetch('/vulnsPoster/status/',{
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then(function(response){
            return response.json().then(function(dataJSON){
                loadAttribute(dataJSON, ddMenu, dd, field, removeAll);
            });
        });
    }
    catch(error){
        setToastrError();
    }
}

//Loads attribute to dropdown

function loadAttribute(data, dropdownMenu, dropdown, dataField, removeAll){
    let firstAttr = (JSON.stringify(data[0][dataField])).replace(/['"]+/g, '');
    $(data).each(function(field){
        let attr = JSON.stringify(data[field][dataField])
        $(dropdownMenu).append('<a class="dropdown-item">'+attr.replace(/['"]+/g, '')+'</a>');
    });
    selectAttribute(dropdownMenu, dropdown);
    if(removeAll){
        removeAttribute(dropdownMenu);
    }
    else{
        setValTextDropdown(dropdown,firstAttr);
    }
}

function selectAttribute(dropdownMenu, dropdown){
    $(`${dropdownMenu} a`).on('click',function(){
        let selectedItem = ($(this).text());
        setValTextDropdown(dropdown, selectedItem);
    });
}

function setValTextDropdown(dropdown, value){
    $(dropdown).val(value);
    $(dropdown).text(value);
}

function removeAttribute(dropdownMenu){
    $(dropdownMenu).children('a:first').remove();
}

function appendDataTable(dtName){
    $('.table-responsive').append(`<table id=${dtName} class="table table-striped" style="width:100%"></table>`);
}

function clearControls(mainControl){
    $(mainControl).find('.modal-body input').val('');
    $(mainControl).find('.modal-body textarea').val('');
    $(mainControl).find('.modal-body dropdown').val('');
}

function formatDate(date){
    try{
        let newDate = new Date(date);
        return newDate.toISOString().split('T')[0];
    }
    catch(error){
        setToastrError();
    }
}

function setToastrOptions(){
    toastr.options = {
        "debug": false,
        "positionClass": "toast-bottom-right",
        "onclick": null,
        "fadeIn": 100,
        "fadeOut": 200,
        "timeOut": 2000,
        "extendedTimeOut": 4000,
        "preventDuplicates": true
      }
}

function setToastrError(){
    toastr.error("An error has ocurred", "Error"); //Should particular errors be logged?
}

function setToastrSuccess(){
    toastr.success("Task executed successfully","Success");
}

function setLoader(control){
    $(control).waitMe({
        effect : 'rotation',
        text : '',
        maxSize : '60',
        color: "#1b75be",
        waitTime : -1,
        textPos : 'vertical',
        fontSize : '',
        source : '',
    })
}

function setLoaderHide(control){
    $(control).waitMe("hide");
}

//Enable or disable datetimepickers

$("#flexCheckDates").on('click',function(){
    let check = $(this).prop('checked');
    if(check == true) {
        enableOrDisableDateTimePicker('#txtDateFrom', true)
        enableOrDisableDateTimePicker('#txtDateTo', true)
    } else {
        enableOrDisableDateTimePicker('#txtDateFrom', false)
        enableOrDisableDateTimePicker('#txtDateTo', false)
    }
 });

function setDateTimePicker(dtpicker){
    $(dtpicker).datetimepicker({
        icons:{
            today: "fa fa-clock text-dark",
            clear: "fa fa-trash text-dark",
            close: "fa fa-circle-xmark text-dark"
        },
        "allowInputToggle": true,
        "showClose": true,
        "showClear": true,
        "showTodayButton": true,
        "format": "MM/DD/YYYY"
    })
}

$('#txtDateFrom').datetimepicker({
    icons:{
        today: "fa fa-clock text-dark",
        clear: "fa fa-trash text-dark",
        close: "fa fa-circle-xmark text-dark"
    },
    "allowInputToggle": true,
    "showClose": true,
    "showClear": true,
    "showTodayButton": true,
    "format": "MM/DD/YYYY",
    "maxDate": moment()
    }).on('dp.change',function(e){
        $('#txtDateTo').data("DateTimePicker").minDate(e.date)
});

$('#txtDateTo').datetimepicker({
    icons:{
        today: "fa fa-clock text-dark",
        clear: "fa fa-trash text-dark",
        close: "fa fa-circle-xmark text-dark"
    },
    "allowInputToggle": true,
    "showClose": true,
    "showClear": true,
    "showTodayButton": true,
    "format": "MM/DD/YYYY",
    "minDate": moment()  
});

function enableOrDisableDateTimePicker(datePicker, isDisabled){
    $(datePicker).prop('disabled', isDisabled);
}

function clearDropdown(dropdown){
    $(dropdown).empty();
}

function sleep(time){
    return new Promise((resolve) => setTimeout(resolve, time));
}

$('#sidebarCollapse').on('click', function() {
    $('#sidebar').toggleClass('active');
});

$("#dropdownVulnTypeMenu").on('click', function(){
    let selectedItem = $("#ddVulnType").val();
    $('#colCreateVuln > button').attr('id',`btnNewVuln${selectedItem}`);
});
