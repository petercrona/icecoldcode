export const mkApi = (resource, base = "/api") => ({
  create: (dto) =>
    fetch(`${base}${resource}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(dto),
    }).then((x) => {
      return x.ok ? x.json() : Promise.reject();
    }),
  list: () =>
    fetch(`${base}${resource}`).then((x) => {
      return x.ok ? x.json() : Promise.reject();
    }),
  remove: (id) =>
    fetch(`${base}${resource}${id ? `/${id}` : ""}`, {
      method: "DELETE",
    }).then((x) => {
      return x.ok ? Promise.resolve(x) : Promise.reject();
    }),
});

export const formDataToJson = (formData) =>
  [...formData.entries()].reduce((res, [key, value]) => {
    if (key.endsWith("[]")) {
      const actualKey = key.slice(0, -2);
      if (res[actualKey] !== undefined) {
        res[actualKey].push(value);
      } else {
        res[actualKey] = [value];
      }
    } else {
      res[key] = value;
    }
    return res;
  }, {});