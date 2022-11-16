<template id="activity-overview">
  <app-layout>
    <div>
      <div>
        <ul class="activity-overview-list">
          <li v-for="activity in activities">
            {{activity.id}}: User {{activity.userId}} did {{activity.description}} for {{activity.duration}} minutes
          </li>
        </ul>
      </div>
    </div>
  </app-layout>
</template>
<script>
Vue.component("activity-overview", {
  template: "#activity-overview",
  data: () => ({
    activities: [],
  }),
  created() {
    this.fetchActivities();
  },
  methods: {
    fetchActivities: function () {
      axios.get("/api/activities")
          .then(res => this.activities = res.data)
          .catch(() => alert("Error while fetching activities"));
    }
  }
});
</script>