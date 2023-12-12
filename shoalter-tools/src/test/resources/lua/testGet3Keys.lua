-- local sku1 = redis.call('GET', ARGV[1])
-- local sku2 = redis.call('GET', ARGV[2])
-- local sku3 = redis.call('GET', ARGV[3])

local sku1 = KEYS[1]
local sku2 = KEYS[2]
local sku3 = KEYS[3]
local sku4 = KEYS[4]
local sku5 = KEYS[5]
local sku6 = KEYS[6]

-- local share = redis.call('HGET', sku2, "share")
-- local share3 = redis.call('HGET', sku3, "share")

local results = {}
table.insert(results, redis.call('GET', sku1))
table.insert(results, redis.call('GET', sku2))
table.insert(results, redis.call('GET', sku3))
table.insert(results, redis.call('GET', sku4))
table.insert(results, redis.call('GET', sku5))
table.insert(results, redis.call('GET', sku6))
-- table.insert(results, share3)

-- return redis.call('GET', sku1)

return results
