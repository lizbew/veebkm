<template>
  <div :id="itemId">
    <span><a :href="bookmark.url">{{ bookmark.url }}</a></span>
  </div>
</template>

<script>
export default {
  name: 'item',
  props: {
    bookmark: Object,
  },

  computed: {
    itemId: function() {
      return 'bkm-item-' + this.bookmark.id;
    },
  }


}
</script>
