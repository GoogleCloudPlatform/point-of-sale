/**
 * Copyright 2021 Google LLC
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

/**
 * A mixin that is used to update the title of the page from the the different
 * vue components. See https://medium.com/@Taha_Shashtari/the-easy-way-to-change-page-title-in-vue-6caf05006863
 * more details.
 *
 * @param {Object} vm VueModel view instance
 * @return {*}  The VueModel options where the tiles can be fetched from
 */
function getTitle(vm) {
  const {title} = vm.$options;
  if (title) {
    return typeof title === 'function' ? title.call(vm) : title;
  }
}
export default {
  created() {
    const title = getTitle(this);
    if (title) {
      document.title = title;
    }
  },
};
