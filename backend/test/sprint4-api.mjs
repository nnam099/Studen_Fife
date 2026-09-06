import assert from 'node:assert/strict';
const base = process.env.API_URL || 'http://127.0.0.1:3004/api/v1';
let token;
async function request(method, path, body, expected = 200, auth = token) {
  const response = await fetch(base + path, { method, headers: { 'Content-Type': 'application/json', ...(auth ? {Authorization: `Bearer ${auth}`} : {}) }, ...(body ? {body: JSON.stringify(body)} : {}) });
  const data = await response.json();
  assert.equal(response.status, expected, `${method} ${path}: ${JSON.stringify(data)}`);
  return data;
}
const today = new Intl.DateTimeFormat('en-CA', {timeZone:'Asia/Ho_Chi_Minh', year:'numeric', month:'2-digit', day:'2-digit'}).format(new Date());
const day = n => new Date(Date.parse(today) + n * 86400000).toISOString().slice(0,10);
const register = suffix => request('POST', '/auth/register', {email:`sprint4-${Date.now()}-${suffix}@example.com`, password:'Sprint4-test-password', fullName:'Sprint 4 API test'}, 201, null);
token = (await register('a')).accessToken;
const term = await request('POST','/academic-terms',{name:'Sprint4',startDate:day(-30),endDate:day(90)},201);
const otherTerm = await request('POST','/academic-terms',{name:'Other term',startDate:day(-30),endDate:day(90)},201);
const cat = await request('POST','/categories',{name:'Food',type:'expense'},201);
const cat2 = await request('POST','/categories',{name:'Other',type:'expense'},201);
const budget = (periodType,amount,startDate,endDate,academicTermId=term.id) => request('POST','/budgets',{periodType,amount,startDate,endDate,academicTermId,categoryId:cat.id,currency:'VND'},201);
const weekly = await budget('weekly',100000,day(-3),day(3));
const academic = await budget('academic_term',200000,day(-30),day(90));
const monthly = await budget('monthly',900000,day(-10),day(19));
await budget('weekly',1,day(-10),day(-4)); // expired
await budget('weekly',1,day(1),day(7)); // future
await budget('weekly',1,day(-3),day(3),otherTerm.id); // wrong term
const milestone = (title, dueDate, isCompleted=false, academicTermId=term.id) => request('POST','/milestones',{title,dueDate,isCompleted,academicTermId,type:'exam'},201);
await milestone('Past',day(-1));
await milestone('Completed',day(0),true);
await milestone('Wrong term',day(0),false,otherTerm.id);
await milestone('Too late',day(8));
const near = await milestone('Exam in seven days',day(7));
const tx = (amount,extra={}) => request('POST','/transactions',{amount,type:'expense',description:'Sprint4 expense',occurredAt:today,categoryId:cat.id,academicTermId:term.id,...extra},201);
const low = await tx(50000);
assert.deepEqual(await request('GET','/alerts'),[]);
console.log('BELOW THRESHOLD GET /alerts: []');
await tx(50000);
assert.deepEqual(await request('GET','/alerts'),[]);
await tx(999999,{type:'income'});
assert.deepEqual(await request('GET','/alerts'),[]);
const high = await tx(150000);
let alerts = await request('GET','/alerts?status=unread');
assert.equal(alerts.length,2);
for (const [b,excess,percent] of [[weekly,'150.000','150%'],[academic,'50.000','25%']]) {
 const a = alerts.find(a=>a.budget.id===b.id);
 assert.ok(a); assert.equal(a.budget.periodType,b.periodType); assert.equal(a.milestone.id,near.id);
 assert.ok(a.message.includes(excess)); assert.ok(a.message.includes(percent));
}
console.log('MULTI-BUDGET GET /alerts?status=unread:',JSON.stringify(alerts,null,2));
const otherToken = (await register('b')).accessToken;
assert.deepEqual(await request('GET','/alerts',null,200,otherToken),[]);
console.log('OWNERSHIP:', JSON.stringify(await request('PATCH',`/alerts/${alerts[0].id}/read`,{},404,otherToken)));
await request('PATCH',`/alerts/${alerts[0].id}/dismiss`,{},404,otherToken);
await request('GET','/alerts?status=invalid',null,400);
await request('GET','/alerts',null,401,null);
console.log('READ:',JSON.stringify(await request('PATCH',`/alerts/${alerts[0].id}/read`,{})));
assert.equal((await request('GET','/alerts?status=read')).length,1);
console.log('DISMISS:',JSON.stringify(await request('PATCH',`/alerts/${alerts[1].id}/dismiss`,{})));
assert.equal((await request('GET','/alerts?status=dismissed')).length,1);
await request('PATCH',`/transactions/${high.id}`,{amount:160000});
assert.equal((await request('GET','/alerts?status=unread')).length,2);
await request('PATCH',`/transactions/${high.id}`,{categoryId:cat2.id});
assert.equal((await request('GET','/alerts?status=unread')).length,0);
await request('PATCH',`/transactions/${high.id}`,{categoryId:cat.id,occurredAt:day(3)}); // inclusive end
assert.equal((await request('GET','/alerts?status=unread')).length,2);
await request('PATCH',`/transactions/${high.id}`,{type:'income'});
assert.equal((await request('GET','/alerts?status=unread')).length,0);
await Promise.all([tx(60000),tx(60000)]);
assert.equal((await request('GET','/alerts?status=unread')).length,2);
const before = (await request('GET','/transactions')).length;
await tx(1,{categoryId:'00000000-0000-4000-8000-000000000000'}).then(()=>assert.fail('expected 404'), e=>assert.match(e.message,/404/));
assert.equal((await request('GET','/transactions')).length,before);
console.log('PASS: below/equal limit, income, weekly + academic_term, monthly below limit, date windows, term/category isolation, milestone +7, status, ownership, edits, concurrent writes.');
