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

//Create exxxpose.me URLs from text in comments
const comments = document.getElementsByClassName('message-content-text');
                            Array.from(comments).forEach((item, index, arr) => {
                            var text = item.innerHTML;
                            if(text.includes("https://www.exxxpose.me/")){
                            var split = text.split("https://www.exxxpose.me/");
                            var path = split[1].split(" ");
                            comments[index].innerHTML = comments[index].innerHTML.replace("https://www.exxxpose.me/" + path[0],"<a href='" + "https://www.exxxpose.me/" + path[0] + "'>https://www.exxxpose.me/" + path[0] + "</a>");
                            comments[index].innerHTML = comments[index].innerHTML.replace("#comments","");
                            }
                            })

//Create exxxpose.me URLs from text in caption
let object = document.getElementsByClassName('card-notes')[0];
                            let str = object.innerHTML;
                            let doc = new DOMParser().parseFromString(str, 'text/html');
                            const text = doc.querySelector('p').textContent;
                            if(text.includes("https://www.exxxpose.me/")){
                                var split = text.split("https://www.exxxpose.me/");
                                var path = split[1].split(" ");
                                object.innerHTML = object.innerHTML.replace("https://www.exxxpose.me/" + path[0],"<a href='" + "https://www.exxxpose.me/" + path[0] + "'>https://www.exxxpose.me/" + path[0] + "</a>");
                            }