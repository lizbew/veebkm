<template>
<!-- demo root element -->
<div id="demo">
  <form id="search">
    Search <input name="query" v-model="searchQuery">
  </form>
  <div v-for="row in bookmarks">
    <item :bookmark="row"></item>
  </div>

  <!--
  <demo-grid
    :data="bookmarks"
    :columns="gridColumns"
    :filter-key="searchQuery">
  </demo-grid>
  -->
  <form id="add-form">
    <div>
      <label for="new-title">Title</label>
      <input id="new-title" name="title" type="text" v-model="newOne.title">
    </div>
    <div>
      <label for="new-title">Url</label>
      <input id="new-url" name="url" type="text" v-model="newOne.url">
    </div>
    <div>
      <input name="add-btn" type="button" @click="addBookmark" value="Save">
    </div>
  </form>
</div>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'
// import DemoGrid from './DemoGrid';
import Item from './item';
import { veebkmService } from '@/services';

export default {
  name: 'bookmark-list',
  data() {
    return {
      searchQuery: '',
      gridColumns: ['id', 'title', 'url'],
      newOne: {
        title: '',
        url: '',
      }
    }
  },

  computed: mapGetters({
    bookmarks: 'allBookmarks',
  }),

  methods: {
    addBookmark: function() {
      if (!this.newOne.title || !this.newOne.url) {
        alert("must input both name and url");
        return;
      }
      veebkmService.addBookmark(this.newOne.title, this.newOne.url).then((data) => {
        if (data.success){
          this.newOne.title = "";
          this.newOne.url = "";
          this.$store.dispatch('getAllBookmarks')

          alert("OK");
        } else {
          alert("Failed: " + data.error);
        }

      });

    },
  },

  // ready() {}

  created() {
    this.$store.dispatch('getAllBookmarks')
  },

  components: {
    Item,
  },
};
</script>

<style>
body {
  font-family: Helvetica Neue, Arial, sans-serif;
  font-size: 14px;
  color: #444;
}

table {
  border: 2px solid #42b983;
  border-radius: 3px;
  background-color: #fff;
}

th {
  background-color: #42b983;
  color: rgba(255,255,255,0.66);
  cursor: pointer;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;
}

td {
  background-color: #f9f9f9;
}

th, td {
  min-width: 120px;
  padding: 10px 20px;
}

th.active {
  color: #fff;
}

th.active .arrow {
  opacity: 1;
}

.arrow {
  display: inline-block;
  vertical-align: middle;
  width: 0;
  height: 0;
  margin-left: 5px;
  opacity: 0.66;
}

.arrow.asc {
  border-left: 4px solid transparent;
  border-right: 4px solid transparent;
  border-bottom: 4px solid #fff;
}

.arrow.dsc {
  border-left: 4px solid transparent;
  border-right: 4px solid transparent;
  border-top: 4px solid #fff;
}
</style>
