<!--
 Copyright 2021 Google LLC

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->

<template>
  <div>
    <table
      class="table table-striped table-hover table-bordered table-responsive"
      v-if="items.length"
    >
      <thead>
        <tr>
          <th>Name</th>
          <th>Number of Items</th>
          <th>Amount</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in items" v-bind:key="item.id">
          <td>{{ item.item.name }}</td>
          <td v-on:dblclick="toggleEdit(item)">
            <span v-if="!item.editing">{{ item.numberOfItems }}</span>
            <input
              class="form-control"
              type="number"
              min="1"
              v-if="item.editing"
              v-on:blur="toggleEdit(item)"
              v-model="item.numberOfItems"
            />
          </td>
          <td>{{ toCurrency(item.numberOfItems * item.item.price) }}</td>
          <td>
            <button
              type="button"
              class="btn btn-danger"
              v-on:click="removeItem(item)"
            >
              <i class="fa fa-times"></i>
            </button>
          </td>
        </tr>
      </tbody>
    </table>
    <p v-if="!items.length">No items have been added.</p>

    <table class="table">
      <tbody>
        <tr>
          <td>Subtotal:</td>
          <td>{{ toCurrency(subtotal) }}</td>
        </tr>
        <tr>
          <td>Tax:</td>
          <td>{{ toCurrency(tax) }}</td>
        </tr>
        <tr>
          <td>Total:</td>
          <td>{{ toCurrency(total) }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
export default {
  name: "Transaction",
  components: {},
  props: {
    items: {
      type: Array,
      required: true,
    },
  },
  data: () => {
    return {};
  },
  computed: {
    subtotal() {
      var subtotal = 0;
      this.items.forEach(function(item) {
        subtotal += item.item.price * item.numberOfItems;
      });
      return subtotal;
    },
    tax() {
      return this.subtotal * 0.065;
    },
    total() {
      return this.subtotal + this.tax;
    },
  },
  methods: {
    toggleEdit(item) {
      // this.$emit("edit", item);
      item.editing = !item.editing;
    },
    removeItem(item) {
      this.$emit("remove", item);
    },
    toCurrency(number) {
      return new Intl.NumberFormat("en-US", {
        style: "currency",
        currency: "USD",
      }).format(number);
    },
  },
};
</script>

<style scoped>
.table-responsive {
  display: inline-table;
}

.btn {
  padding: 0em 0.25em;
}
</style>
