function updateUserDisplay(data) {
  const local = getUser()

  console.log("local", local)
  console.log("data", data)

  if (local != null) {
    document.getElementById("user_id").innerText = local.id;
    document.getElementById("nickname_dash").innerText = local.nickname;
  }else{
    document.getElementById("user_id").innerText = data.id;
    document.getElementById("nickname_dash").innerText = data.nickname;
  }
}

document.addEventListener("DOMContentLoaded", updateUserDisplay())