redis.call('HDEL', KEYS[1], 'H08880011802_available', 'H08880011802_instockstatus', 'H08880011802_updatestocktime')
redis.call('HMSET', KEYS[1], 'H08880011898_available', '2000')
redis.call('HMSET', KEYS[1], 'H08880011898_instockstatus', 'null')
redis.call('HMSET', KEYS[1], 'H08880011898_updatestocktime', '20240101000001')

return 'success'
