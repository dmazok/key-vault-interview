package com.interview

import spock.lang.Specification
import spock.lang.Subject

class InMemoryKeyVaultTest extends Specification {

    @Subject
    KeyVault keyVault = new InMemoryKeyVault()

    def setup() {
        keyVault.store('key1', 'value1')
        keyVault.store(2, 'value2')
    }

    def 'Should store valid key-value pairs'() {
        when:
        keyVault.store(key, value)

        then:
        keyVault.get(key) == value

        where:
        key    | value
        'key4' | 'value4'
        4      | 'value4'
    }

    def 'Should store new valid value for an existing key and return the previous one'() {
        expect:
        keyVault.store(key, value) == previousValue

        and:
        keyVault.get(key) == value

        where:
        key    | value    | previousValue
        'key1' | 'value5' | 'value1'
        2      | 'value6' | 'value2'
    }

    def 'Should be case-insensitive'() {
        given:
        def value = 'some-value'

        when:
        keyVault.store('SoMe_Key', value)

        then:
        keyVault.get('SOME_KEY') == value
        keyVault.get('some_key') == value
        keyVault.get('SoMe_Key') == value
    }

    def 'Should treat integral numbers of any type as the same key'() {
        given:
        keyVault.store(7, 'seven')

        expect:
        keyVault.get(numberKey) == 'seven'

        where:
        numberKey << [7 as Integer, 7L, (short) 7, (byte) 7, 7.0f, 7.0d,
                      new BigDecimal('7.00'), new BigInteger('7')]
    }

    def 'Should overwrite a number key regardless of the numeric type used'() {
        given:
        keyVault.store(7, 'seven')

        when:
        def previous = keyVault.store(7.0d, 'SEVEN')

        then:
        previous == 'seven'

        and:
        keyVault.get(7L) == 'SEVEN'
    }

    def 'Should keep number and string namespaces independent'() {
        when:
        keyVault.store('11', 'as-string')
        keyVault.store(11, 'as-number')

        then:
        keyVault.get('11') == 'as-string'
        keyVault.get(11) == 'as-number'
    }

    def 'Should not apply the string key length limit to number keys'() {
        given:
        def key = new BigInteger('1' * 30)

        when:
        keyVault.store(key, 'big')

        then:
        keyVault.get(key) == 'big'
    }

    def 'Should reject a null value'() {
        when:
        keyVault.store(key, null)

        then:
        thrown(IllegalArgumentException)

        where:
        key << ['valid-key', 7]
    }

    def 'Should throw exception on missing key fetch attempt'() {
        when:
        keyVault.get(key)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'No such key in vault'

        where:
        key << ['missing-key', 9999999]
    }

    def 'Should address entries by the normalized string key'() {
        given:
        KeyNormalizer keyNormalizer = Mock()
        def vault = new InMemoryKeyVault(keyNormalizer)

        when:
        vault.store('Key', 'value')

        then:
        1 * keyNormalizer.normalize('Key') >> new TypedKey(KeyType.STRING, 'key')

        when:
        def result = vault.get('KEY')

        then:
        1 * keyNormalizer.normalize('KEY') >> new TypedKey(KeyType.STRING, 'key')

        and:
        result == 'value'
    }

    def 'Should address entries by the normalized number key'() {
        given:
        KeyNormalizer keyNormalizer = Mock()
        def vault = new InMemoryKeyVault(keyNormalizer)

        when:
        vault.store(7, 'seven')

        then:
        1 * keyNormalizer.normalize(7) >> new TypedKey(KeyType.NUMBER, '7')

        when:
        def result = vault.get(7.0d)

        then:
        1 * keyNormalizer.normalize(7.0d) >> new TypedKey(KeyType.NUMBER, '7')

        and:
        result == 'seven'
    }

    def 'Should propagate normalizer rejections'() {
        given:
        KeyNormalizer keyNormalizer = Mock()
        def vault = new InMemoryKeyVault(keyNormalizer)

        when:
        vault.store('bad', 'value')

        then:
        1 * keyNormalizer.normalize('bad') >> { throw new IllegalArgumentException('Invalid argument') }

        and:
        thrown(IllegalArgumentException)
    }
}
