// Seed Lab 05 demo API key for Temperature Converter (runs once on empty volume)
db = db.getSiblingDB('tempconverter');
db.api_keys.insertOne({
  keyValue: 'lab05-demo-key',
  clientName: 'Converter Hub Frontend',
  active: true
});
