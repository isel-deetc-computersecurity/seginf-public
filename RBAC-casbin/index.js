//
// following https://github.com/casbin/node-casbin
//

const { newEnforcer } = require('casbin');

// PDP - decide if request is accepted or denied
const pdp = async function(s, o, a) {
  const enforcer = await newEnforcer('basic_model.conf', 'basic_policy.csv');
  r = await enforcer.enforce(s, o, a);
  return {res: r, sub: s, obj: o, act: a};
}

// Do action or not, based on decision
const execute = function(decision) {
  console.log(decision);
  if (decision.res == true) {
    console.log("permit operation")
  } else {
    console.log("deny operation")
  }  
}

pdp('alice', 'data1', 'read').then(execute);
pdp('alice', 'data1', 'write').then(execute);
pdp('bob', 'data2', 'write').then(execute);
