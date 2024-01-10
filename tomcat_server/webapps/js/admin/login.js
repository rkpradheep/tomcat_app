
    function login() {
    const name = document.getElementById('name').value
    const password = document.getElementById('password').value
    if(name == undefined || name.length <1 || password == undefined || password.length <1)
    {
    alert("Invalid input")
    return
    }

        var payload = {
        "name" : name,
        "password" : password
        }
          fetch( "/api/v1/admin/authenticate", {
               method: "POST",
                headers: {
                    'Content-Type' : 'application/json'
                },
                body: JSON.stringify(payload)
          })
          .then(response =>
          {
          return response.text();
          }
          ).then(data=> {
            const res = JSON.parse(data);
            if(res["message"] != "success")
            {
                alert("Invalid credentials")
                return;
            }
            if(new URLSearchParams(window.location.search).get('post'))
            {
                window.close();
             }
            const redirectURI =  new URLSearchParams(window.location.search).get('redirect_uri');
            window.open (redirectURI, "_self");

          }).catch(error => {
             alert("Something went wrong. Server might be down");
          });
    }
