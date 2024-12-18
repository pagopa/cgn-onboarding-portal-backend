/*
 *
 *  Copyright 2015 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package springfox.documentation.spring.web.paths;

import org.apache.http.util.TextUtils;

import javax.servlet.ServletContext;

public class RelativePathProvider
        extends DefaultPathProvider {
    public static final String ROOT = "/";
    private final ServletContext servletContext;

    public RelativePathProvider(ServletContext servletContext) {
        super();
        this.servletContext = servletContext;
    }

    protected String applicationPath() {
        return TextUtils.isEmpty(servletContext.getContextPath()) ? ROOT:servletContext.getContextPath();
    }

    @Override
    protected String getDocumentationPath() {
        return ROOT;
    }
}