# HLL Client Demo

__HLL API__ Client library integration and sample code written in JavaFX.

Demo shows how to use classic synchronous and optionally asynchronous HLL API calls.

__Requirements:__
* Demo requires Green Screens Terminal Service
* Browser extension (link available from main login page)
* Browser integration module

__Steps:__
1. Go to the extension options page and enable HLL (Link tab)
2. Create terminal session with named id (letter from A..Z)
3. Start demo, enter named id and select function 1 (connect)

__NOTE:__ Function 200 is available from browser extension version 4.2.

__Offline test:__
To test/debug without browser extension, use browser integration module - hll.exe app with environment variable GOCONSOLE=true, then paste JSON responses inside console.

Copyright (C) 2015, 2020  Green Screens Ltd.