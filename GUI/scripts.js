function submitForm(id) {
    vals = {}
    document.getElementById(id).querySelectorAll("select").forEach((e) => {vals[e.id] = e.value})
    console.log(vals);
}