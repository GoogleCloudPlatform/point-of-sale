<!--
 Copyright 2022 Google LLC

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
  <div class="container">
    <div class="row" id="pos">
      <div class="col-md-8 col-md-offset-2">
        <div class="header-container">
          <h1 class="h1-flex">Point of Sale!</h1>
        </div>
        <div class="row">
          <div class="col-md-6">
            <transaction-view
              :items="currentItems"
              @remove="removeItem"
              @edit="toggleEdit"
              @pay="pay"
              @clear="clear"
            ></transaction-view>
          </div>
          <div class="col-md-6">
            <item-list :items="items" @add="addItem"></item-list>
          </div>
        </div>
      </div>
    </div>
    <div v-if="hasBill" class="bill-container">
      <h2>Bill:</h2>
      <pre class="print-container">{{ bill }}</pre>
    </div>
  </div>
</template>

<script>
import RetailEdgeAppApi from '@/services/RetailEdge';
import ItemList from './ItemList.vue';
import TransactionView from './TransactionView.vue';

export default {
  name: 'MainPage',
  title: 'Retail Edge POS',
  components: {
    ItemList,
    TransactionView,
  },
  props: {},
  data: () => {
    return {
      items: [],
      lineItems: [],
      storeTypes: [],
      printedBill: null,
    };
  },
  computed: {
    currentItems() {
      return [...this.lineItems];
    },
    bill() {
      return this.printedBill;
    },
    hasBill() {
      return !!this.bill;
    },
  },
  async created() {
    await this.loadStoreTypes();
    await this.loadItems();
  },
  methods: {
    async loadStoreTypes() {
      const response = await RetailEdgeAppApi.types();
      if (response.status !== 200 && response.status !== 204) {
        this.notifyFailure('Failed to load store types!');
        return;
      }
      const data = await response.json();
      this.storeTypes = data.reduce((acc, curr) => {
        return [{value: curr}, ...acc];
      }, []);
    },
    async loadItems() {
      const response = await RetailEdgeAppApi.items();
      if (response.status !== 200 && response.status !== 204) {
        this.notifyFailure('Failed to load items!');
        return;
      }
      const data = await response.json();
      this.items = data.reduce((acc, curr) => {
        acc = [curr, ...acc];
        return acc;
      }, []);
    },
    addItem(item) {
      let found = false;
      for (let i = 0; i < this.lineItems.length; i++) {
        if (this.lineItems[i].item === item) {
          this.lineItems[i].numberOfItems++;
          found = true;
          break;
        }
      }
      if (!found) {
        this.lineItems.push({item: item, numberOfItems: 1, editing: false});
      }
    },
    removeItem(item) {
      for (let i = 0; i < this.lineItems.length; i++) {
        if (this.lineItems[i] === item) {
          this.lineItems.splice(i, 1);
          break;
        }
      }
    },
    toggleEdit(item) {
      item.editing = !item.editing;
    },
    async pay(total) {
      if (this.lineItems.length == 0) {
        this.notifyWarning('No items in cart!');
        return;
      }
      const cartItems = this.lineItems.reduce((acc, curr) => {
        acc = [
          {
            itemId: curr.item.id,
            itemCount: curr.numberOfItems,
          },
          ...acc,
        ];
        return acc;
      }, []);
      const payRequest = {
        paidAmount: total,
        type: 'CASH',
        items: cartItems,
      };
      const response = await RetailEdgeAppApi.pay(payRequest);
      if (response.status !== 200 && response.status !== 204) {
        this.notifyFailure('Payment API failed!');
        return;
      }
      const responseData = await response.json();
      if (responseData.status !== 'SUCCESS') {
        this.notifyFailure('Payment attempt failed!');
        return;
      }
      this.notifySuccess('Payment successfull!');
      this.printedBill = responseData.printedBill;
      setTimeout(() => {
        this.clearBill();
      }, 3000);
    },
    clear() {
      this.lineItems = [];
      this.clearBill();
    },
    clearBill() {
      this.printedBill = null;
    },
    notifySuccess(message) {
      this.notify(message, 'success');
    },
    notifyWarning(message) {
      this.notify(message, 'default');
    },
    notifyFailure(message) {
      this.notify(message, 'error');
    },
    notify(message, type) {
      this.$toast.open({
        message,
        type,
        position: 'top',
        duration: 1500,
      });
    },
  },
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.col-md-8 {
  max-width: 100%;
  flex: 100%;
}

.h1-flex {
  flex: 1;
}

.header-container {
  display: flex;
  margin-top: 18px;
  margin-bottom: 24px;
}

.bill-container {
  margin-top: 28px;
}

.print-container {
  background-color: beige;
  text-align: center;
  border-radius: 30px;
  padding-bottom: 20px;
  padding-top: 20px;
}
</style>
