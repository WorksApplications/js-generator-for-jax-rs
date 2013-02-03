# a Maven plugin to generate JavaScript for JAX-RS
This Maven plugin helps you to generate JavaScript to commnicate with server.
It parses your controllers, and generate JavaScript files to send Ajax request.

Here is an example. Generated JavaScript depends on [jQuery](http://jquery.com/) and [RequireJS](http://requirejs.org/).

```javascript
define(['jquery', 'exports'], function($, exports) {
  'use strict';
  var baseURL = $('meta[name="app-data"]').data('context-path') + '/resources';
  exports.load = function (message) {
    return $.ajax({
        cache: false,
        url: baseURL + '/sample/',
        type: 'get',
        data: {'message':message}
    }).promise();
  };
});
```

# How to use

## modify your pom.xml
`js-generator-for-jax-rs` is implemented as a Maven plugin. You have to modify your `pom.xml` to use it.
This plugin will parse classes in `packageName`, and output JavaScript files into `outputDirectory`.

```xml
    <build>
        <plugins>
            <plugin>
                <groupId>jp.co.worksap.jax_rs</groupId>
                <artifactId>js-generator-for-jax-rs</artifactId>
                <version>1.0.0</version>
                <configuration>
                    <packageName>jp.co.worksap.sample</packageName>
                    <outputDirectory>target/js/jax-rs</outputDirectory>
                </configuration>
                <executions>
                    <execution>
                        <phase>process-classes</phase>
                        <goals>
                            <goal>generate</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            ...
        </plugins>
        ...
    </build>
```

## ignore generated code
Generated code should not be stored in version management system like git.
So it is good to put them into `target` directory.

If you cannot, please modify `.gitignore` to ignore generated code.

## kick Maven to generate JavaScript
In this case, `process-sources` goal will generate JavaScript into `target/js/jax-rs`.
You can change goal by `phase` configuration.

## add meta tag into HTML
To tell your context path, please add `meta` tag into your HTML.
JavaScript will send Ajax request to `${context-path}/resources/${specified-path}`.

```html
<meta name="app-data" data-context-path="context-path">
```


# History

## 0.1

* First release

## 0.2

* Added README.md for user
* Supported Maven sub module
* Avoided JSLint warning for ` +''`

## 0.3

* Supported using outer artifact as returned value

## 0.4

* Added module name for each API.js

## 0.5

* Switched to use data-attribute to decide context path instead of location.pathname

## 0.6 

* Removed module name from generated code

## 1.0.0

* Published at GitHub


# License

Copyright 2013 Works Applications. Co.,Ltd.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
