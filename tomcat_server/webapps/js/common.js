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


 async function copyToClipboard(textToCopy) {
     if (false && navigator.clipboard && window.isSecureContext) {
         await navigator.clipboard.writeText(textToCopy);
     } else {

         const textArea = document.createElement("textarea");
         textArea.value = textToCopy;

         textArea.style.position = "absolute";
         textArea.style.left = "-999999px";

         document.body.prepend(textArea);
         textArea.select();

         try {
             document.execCommand('copy');
         } catch (error) {
             console.error(error);
         } finally {
             textArea.remove();
         }
     }
 }