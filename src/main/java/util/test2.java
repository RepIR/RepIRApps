/*
 * Copyright 2014 jeroen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package util;

import io.github.repir.tools.lib.ArrayTools;
import io.github.repir.tools.lib.BoolTools;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.Words.englishStemmer;
import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
/**
 *
 * @author jeroen
 */
public class test2 {
   public static final Log log = new Log( test2.class );

    public static void main(String[] args) {
        Log.out("%s", englishStemmer.get().stem("u.s."));
    }
}
