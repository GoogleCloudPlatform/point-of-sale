<template>
  <div class="container">
    <div class="row" id="pos">
      <div class="col-md-8 col-md-offset-2">
        <h1>Point of Sale</h1>
        <div class="row">
          <div class="col-md-6">
            <transaction
              :items="currentItems"
              @remove="removeItem"
              @edit="toggleEdit"
            ></transaction>
          </div>
          <div class="col-md-6">
            <item-list :items="items" @add="addItem"></item-list>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import ItemList from "./ItemList.vue";
import Transaction from "./Transaction.vue";

export default {
  name: "Main",
  title: 'Retail Edge POS',
  components: {
    ItemList,
    Transaction,
  },
  props: {},
  data: () => {
    return {
      title: 'Foo Page',
      items: [
        { id: 1, name: "Apple", price: 2.99 },
        { id: 2, name: "Orange", price: 0.99 },
        { id: 3, name: "Banana", price: 5.99 },
        { id: 4, name: "TV", price: 199.99 },
        { id: 5, name: "X-Box One", price: 299.99 },
        { id: 6, name: "iPhone 6 Plus", price: 299.99 },
        { id: 7, name: "Cup", price: 3.99 },
        { id: 8, name: "Yogurt", price: 0.49 },
        { id: 9, name: "Hat", price: 9.99 },
      ],
      lineItems: [],
    };
  },
  computed: {
    currentItems() {
      return [...this.lineItems];
    },
  },
  created() {},
  methods: {
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
        this.lineItems.push({ item: item, numberOfItems: 1, editing: false });
      }
    },
    removeItem(item) {
      for (var i = 0; i < this.lineItems.length; i++) {
        if (this.lineItems[i] === item) {
          this.lineItems.splice(i, 1);
          break;
        }
      }
    },
    toggleEdit(item) {
      item.editing = !item.editing;
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
</style>
