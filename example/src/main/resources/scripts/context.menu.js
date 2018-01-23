//(function() {

//  "use strict";

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  //
  // H E L P E R    F U N C T I O N S
  //
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  /**
   * Function to check if we clicked inside an element with a particular class
   * name.
   *
   * @param {Object} e The event
   * @param {String} className The class name to check against
   * @return {Boolean}
   */
  function clickInsideElement( e, className ) {
    var el = e.srcElement || e.target;

    if ( el.classList.contains(className) ) {
      return el;
    } else {
      while ( el = el.parentNode ) {
        if ( el.classList && el.classList.contains(className) ) {
          return el;
        }
      }
    }

    return false;
  }




  /**
   * Get's exact position of event.
   *
   * @param {Object} e The event passed in
   * @return {Object} Returns the x and y position
   */
  function getPosition(e) {
    var posx = 0;
    var posy = 0;

    if (!e) var e = window.event;

    if (e.pageX || e.pageY) {
      posx = e.pageX;
      posy = e.pageY;
      //console.log("pageX,pageY");
    } else if (e.clientX || e.clientY) {
      posx = e.clientX + document.body.scrollLeft + document.documentElement.scrollLeft;
      posy = e.clientY + document.body.scrollTop + document.documentElement.scrollTop;
      //console.log("clientX,clientY");
    }
      //console.log(posx,posy);

    return {
      x: posx,
      y: posy
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  //
  // C O R E    F U N C T I O N S
  //
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  /**
   * Variables.
   */
  var contextMenuClassName = "context-menu";
  var contextMenuItemClassName = "context-menu__item";
  var contextMenuLinkClassName = "context-menu__link";
  var contextMenuActive = "context-menu--active";

  var taskItemClassName = "kj-row";
  var taskItemInContext;

  var pasteItemClassName = "kj-row";
  var pasteItemInContext = null;

  var clickCoords;
  var clickCoordsX;
  var clickCoordsY;

  var menu = document.querySelector("#context-menu");
  var menuItems = menu.querySelectorAll(".context-menu__item");
  var menuState = 0;
  var menuWidth;
  var menuHeight;
  var menuPosition;
  var menuPositionX;
  var menuPositionY;

  var windowWidth;
  var windowHeight;

  /**
   * Initialise our application's code.
   */
  function initAll(contextMenuAction,imagePasteAction) {
    console.log("stating contextMenu init");
    if (imagePasteAction)
        pasteListener(imagePasteAction);
    if (contextMenuAction) {
        contextListener();
        clickListener(contextMenuAction);
        keyupListener();
        resizeListener();
    }
  }

  /**
   * Listens for contextmenu events.
   */
  function contextListener() {
    document.addEventListener( "contextmenu", function(e) {
    console.log("context button click ");
      taskItemInContext = clickInsideElement( e, taskItemClassName );

      if ( taskItemInContext ) {
        e.preventDefault();
        toggleMenuOn();
        positionMenu(e);
      } else {
        taskItemInContext = null;
        toggleMenuOff();
      }
    });
  }

  /**
   * Listens for click events.
   */
  function clickListener(contextMenuAction) {
    document.addEventListener( "click", function(e) {
    console.log("context listened click ");

      var clickeElIsLink = clickInsideElement( e, contextMenuLinkClassName );
      var pasteItemInContext = clickInsideElement( e, pasteItemClassName ); //KKJ_ADD

      if ( clickeElIsLink ) {
        e.preventDefault();
        //menuItemListener( clickeElIsLink );
        contextMenuAction( taskItemInContext , clickeElIsLink.getAttribute("data-action"),e);
      } else {
        var button = e.which || e.button;
        if ( button === 1 ) {
          toggleMenuOff();
        }
      }
    });
  }

  /**
   * Listens for paste events.
   */

   function getPasteImage(pasteCallback) {
      // use event.originalEvent.clipboard for newer chrome versions
      var items = (event.clipboardData  || event.originalEvent.clipboardData).items;
      //console.log(JSON.stringify(items)); // will give you the mime types
      // find pasted image among pasted items
      var blob = null;
      for (var i = 0; i < items.length; i++) {
        if (items[i].type.indexOf("image") === 0) {
          blob = items[i].getAsFile();
        }
      }
      // load image if there is a pasted image
      if (blob !== null) {
        var reader = new FileReader();
        reader.onload = function(event) {
          pasteCallback (event.target.result, event); // data url!
        };
        reader.readAsDataURL(blob);
      }
    }

  function pasteListener(imagePasteAction) {

    //catch all paste event
    document.addEventListener( "paste", function(e) {
        console.log("paste event captured");

      //pasteItemInContext = clickInsideElement( e, pasteItemClassName );
      var clickeElIsLink = true ; // always call menuPasteAction


      if ( clickeElIsLink ) {
        //e.preventDefault();
        //menuItemListener( clickeElIsLink );
        getPasteImage(imagePasteAction);
      }
    });
  }

  /**
   * Listens for keyup events.
   */
  function keyupListener() {
    window.onkeyup = function(e) {
      if ( e.keyCode === 27 ) {
        toggleMenuOff();
      }
    }
  }

  /**
   * Window resize event listener
   */
  function resizeListener() {
    window.onresize = function(e) {
      toggleMenuOff();
    };
  }


  function hideMenuItem(action) {
    for (var index = 0; index < menuItems.length; index++) {
      var el = menuItems[index]
      if (el.firstElementChild.getAttribute("data-action") == action)
         el.classList.remove( "context-menu__item--active" );
     };
  }


  function filterContextMenu(mode,objClassname,docType) {

//console.log(mode,objClassname,docType)
     Array.prototype.forEach.call(menuItems, function(el) {
      el.classList.remove( "context-menu__item--active" );
     });

     if (mode == "normal") {  //kj-card-normal or kj-card-test
        //Array.prototype.forEach.call(menuItems, function(el) {
        for (var index = 0; index < menuItems.length; index++) {
          var el = menuItems[index]
//console.log(mode,el.firstElementChild.getAttribute("data-action"))
          if (el.firstElementChild.getAttribute("data-action") == "newCard" ||
              el.firstElementChild.getAttribute("data-action") == "newTestCard" ||
              el.firstElementChild.getAttribute("data-action") == "Sepa" ||
              el.firstElementChild.getAttribute("data-action") == "pageAdd" ||
              el.firstElementChild.getAttribute("data-action") == "imageAdd" ||
              el.firstElementChild.getAttribute("data-action") == "flipAdd" ||
              el.firstElementChild.getAttribute("data-action") == "optAdd" ||
              el.firstElementChild.getAttribute("data-action") == "typeTestAdd" ||
              el.firstElementChild.getAttribute("data-action") == "typeTestRes" ||
              el.firstElementChild.getAttribute("data-action") == "typeTestBranch" ||
              el.firstElementChild.getAttribute("data-action") == "Delete"
          ) el.classList.add ("context-menu__item--active") ;
         }
        //});
     }

     if (mode == "type") { //kj-card-type
        for (var index = 0; index < menuItems.length; index++) {
          var el = menuItems[index]
          if (el.firstElementChild.getAttribute("data-action") == "pageAdd" ||
              el.firstElementChild.getAttribute("data-action") == "imageAdd" ||
              el.firstElementChild.getAttribute("data-action") == "Delete"
          ) el.classList.add ("context-menu__item--active") ;
        };
     }

     if (objClassname != null && objClassname.indexOf("awi-typecreate-page") >= 0) {
        hideMenuItem("typeTestAdd")
        hideMenuItem("Delete")
      }

/* Why ???
     if (objClassname != null && objClassname.indexOf("awi-typetest-page") >= 0) {
        console.log("hide typeTestBranch ")
        hideMenuItem("typeTestBranch")
      }
*/


     if (docType != null && docType.indexOf("awi-typecreate-page") < 0) {
        hideMenuItem("newTestCard")
        hideMenuItem("typeTestAdd")
        hideMenuItem("typeTestRes")
        hideMenuItem("typeTestBranch")
      }



  }
  /**
   * Turns the custom context menu on.
   */
  function toggleMenuOn() {
    if ( menuState !== 1 ) {
      menuState = 1;
      menu.classList.add( contextMenuActive );
    }
  }

  /**
   * Turns the custom context menu off.
   */
  function toggleMenuOff() {
    if ( menuState !== 0 ) {
      menuState = 0;
      menu.classList.remove( contextMenuActive );
    }
  }

  /**
   * Positions the menu properly.
   *
   * @param {Object} e The event
   */
   /*
  function positionMenu(e) {
    clickCoords = getPosition(e);
    clickCoordsX = clickCoords.x;
    clickCoordsY = clickCoords.y;

    menuWidth = menu.offsetWidth + 4;
    menuHeight = menu.offsetHeight + 4;

    windowWidth = window.innerWidth;
    windowHeight = window.innerHeight;

    if ( (windowWidth - clickCoordsX) < menuWidth ) {
      menu.style.left = windowWidth - menuWidth + "px";
    } else {
      menu.style.left = clickCoordsX + "px";
    }

    if ( (windowHeight - clickCoordsY) < menuHeight ) {
      menu.style.top = windowHeight - menuHeight + "px";
    } else {
      menu.style.top = clickCoordsY + "px";
    }
  }
  */

    function positionMenu(e) {
      clickCoords = getPosition(e);
      menu.style.left = clickCoords.x + "px";
      menu.style.top  = clickCoords.y + "px";
    }

  /**
   * Dummy action function that logs an action when a menu item link is clicked
   *
   * @param {HTMLElement} link The link that was clicked
   */
   /* ---------------Not used anymore . replaced by menuClickAction
  function menuItemListener( link ) {
    console.log( "Task ID - " + taskItemInContext.getAttribute("data-id") + ", Task action - " + link.getAttribute("data-action"));
    toggleMenuOff();
  }
  */

  /**
   * Run the app. changed init by quiznews.scala
   * initContextMenu(menuAction(target eleement, action-id));
   */
  //init();

// })();