//Js to debug in webbrowser

//Hide Header

var style = document.createElement('style'); style.innerHTML = 'html{-webkit-tap-highlight-color: transparent;} main{padding-top: 20px !important;} .mobile-grid-container{margin-top: -20px !important;}';
                        document.getElementsByClassName('header-mobile')[0].style.display = 'none';
                        document.head.appendChild(style);

//Hide Header (Viewer)

var style = document.createElement('style'); style.innerHTML = 'html{-webkit-tap-highlight-color: transparent;} main{padding-top: 20px !important;} .mobile-grid-container{margin-top: -20px !important;}';
                        document.getElementsByClassName('header-mobile')[0].style.display = 'none';
                        document.getElementsByTagName('footer')[0].style.display = 'none';
                        document.head.appendChild(style);


// On Messages

var style = document.createElement('style'); style.innerHTML = '.header-mobile{display: none;} .mobile-box{top: 60px !important;height: calc(100% - 60px)!important;}.message-headers{top: 0px !important;}.message-headers{width: calc(100% - 40px)!important;} .message-headers span a{display: none !important;} .message-text-content{ height: calc(100% - 92px) !important; position: fixed !important; bottom: 0 !important; top: 0 !important; } .disclaimer{margin-top: -20px !important;position: absolute !important; bottom: 60px !important;}';
                            document.head.appendChild(style);


// Add style
var style = document.createElement('style'); style.innerHTML = '';
                            document.head.appendChild(style);


