
document.getElementById('name').value = "test";
document.getElementById('password').value = "test@123";
login();

    function login() {
    const name = document.getElementById('name').value;
    const password = document.getElementById('password').value;

        var payload = {
        "name" : name,
        "password" : password
        }
          fetch( "/api/v1/authenticate", {
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
            const redirectURI =  new URLSearchParams(window.location.search).get('redirect_uri');
            window.open (redirectURI, "_self");

          }).catch(error => {
             alert("Something went wrong. Server might be down");
          });
    }
