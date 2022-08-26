function validateLogin(){
    if(checkEmptyInputs()){
        setLoader('#cardLogin');
        fetch('/validateLogin/',{
            method: 'POST',
            body: JSON.stringify(getLoginJSON()),
            headers: {
                'Content-Type': 'application/json'
            }
        }).then(function(response){
            if(response.ok){
                sleep(3000).then(()=>{
                    setLoaderHide('#cardLogin');
                    document.location.href='/home/' //Store in env file
                }); 
            }
        });
    }
}

function getLoginJSON(){
    let loginJSON = {
        "username": $('#txtUsername').val(),
        "password": $('#txtPassword').val()
    }
    return loginJSON;
}

function checkEmptyInputs(){
    let empty = true
    $('input').each(function(){
        if($(this).val()==""){
            empty = false
            $(this).addClass('is-invalid');
            $(`#lbl${$(this).attr('id')}`).removeClass('d-none');
            $('#'+$(this).attr('id').replace('txt','form')).removeClass('mb-2');
        }
    })
    return empty
}

$('input').on('change',function(){
    $(this).removeClass('is-invalid');
    $(`#lbl${$(this).attr('id')}`).addClass('d-none');
    $('#'+$(this).attr('id').replace('txt','form')).addClass('mb-2');
})

$('#btnLogin').on('click',function(){
    validateLogin();
})

function setLoader(control){
    $(control).waitMe({
        effect : 'bounce',
        text : '',
        maxSize : '80',
        color: "#1b75be",
        waitTime : -1,
        textPos : 'end',
        fontSize : '',
        source : '',
    })
}

function setLoaderHide(control){
    $(control).waitMe("hide");
}

function sleep(time){
    return new Promise((resolve) => setTimeout(resolve, time));
}