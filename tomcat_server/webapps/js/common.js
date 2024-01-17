 function handleRedirection(errorResponse)
 {
     if(errorResponse["code"] != "authentication_needed")
     {
        return false;
     }
  let screenX = screen.width / 2 - 325;
  let screenY = screen.height / 2 - 400;
  var popupOptions = `width=650,height=750,top=${screenY},left=${screenX}`;
  window.open(errorResponse['redirect_uri'], "Re-Auth", popupOptions);

      //location.reload();
    return true;
 }

         function getElementValue(elementID) {
             return document.getElementById(elementID).value;
         }

         function setElementValue(elementID, value) {
             document.getElementById(elementID).value = value;;
         }

         function hideElement(elementID) {
             document.getElementById(elementID).style.display = "none";
         }

         function unHideElement(elementID) {
             document.getElementById(elementID).style.display = "block";
         }

         function setElementChecked(elementID, value) {
             return document.getElementById(elementID).checked = value;
         }