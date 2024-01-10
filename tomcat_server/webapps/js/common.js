 function handleRedirection(errorResponse)
 {
     if(errorResponse["code"] != "authentication_needed")
     {
        return false;
     }
  var popupOptions = "width=650,height=750,toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no";
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