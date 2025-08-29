import React, { useState, useEffect } from 'react';
import './App.css';

interface ConversionResult {
  result: number;
}

interface Currency {
  code: string;
  name: string;
  rate?: number;
}

const App: React.FC = () => {
  const [selectedDate, setSelectedDate] = useState<string>('');
  const [selectedCurrency, setSelectedCurrency] = useState<string>('');
  const [amount, setAmount] = useState<string>('');
  const [result, setResult] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [conversionType, setConversionType] = useState<string>('');
  const [currencies, setCurrencies] = useState<Currency[]>([]);
  const [isLoadingCurrencies, setIsLoadingCurrencies] = useState<boolean>(true);
  const [currencyError, setCurrencyError] = useState<string>('');

  // Get current year and set max date to end of current year
  const currentYear = new Date().getFullYear();
  const maxDate = `${currentYear}-12-31`;

  // Fetch currencies on component mount
  useEffect(() => {
    fetchCurrencies();
  }, []);

  // Fetch currencies whenever date changes
  useEffect(() => {
    if (selectedDate) {
      fetchCurrenciesForDate(selectedDate);
    }
  }, [selectedDate]);

  const fetchCurrencies = async () => {
    try {
      setIsLoadingCurrencies(true);
      setCurrencyError('');
      
      const today = new Date().toISOString().split('T')[0]; // YYYY-MM-DD format
      const response = await fetch(`http://localhost:8081/currencies?date=${today}`);
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      const currenciesData: Currency[] = await response.json();
      setCurrencies(currenciesData);
      
      // Set default currency if available
      if (currenciesData.length > 0) {
        setSelectedCurrency(currenciesData[0].code);
      }
      
      console.log('Fetched currencies for today:', currenciesData);
      
    } catch (error) {
      console.error('Error fetching currencies:', error);
      setCurrencyError('Failed to load currencies. Please refresh the page.');
      
      // Fallback to default currencies
      const fallbackCurrencies: Currency[] = [
        { code: 'USD', name: 'US Dollar' },
        { code: 'EUR', name: 'Euro' },
        { code: 'TRY', name: 'Turkish Lira' }
      ];
      setCurrencies(fallbackCurrencies);
      setSelectedCurrency('USD');
    } finally {
      setIsLoadingCurrencies(false);
    }
  };

  const fetchCurrenciesForDate = async (date: string) => {
    try {
      setIsLoadingCurrencies(true);
      setCurrencyError('');
      
      console.log('Fetching currencies for date:', date);
      const response = await fetch(`http://localhost:8081/currencies?date=${date}`);
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      const currenciesData: Currency[] = await response.json();
      setCurrencies(currenciesData);
      
      // Keep the same currency if it exists in the new list, otherwise select the first one
      if (currenciesData.length > 0) {
        const currentCurrencyExists = currenciesData.some(c => c.code === selectedCurrency);
        if (!currentCurrencyExists) {
          setSelectedCurrency(currenciesData[0].code);
        }
      } else {
        setSelectedCurrency('');
      }
      
      console.log('Fetched currencies for date', date, ':', currenciesData);
      
    } catch (error) {
      console.error('Error fetching currencies for date', date, ':', error);
      setCurrencyError('Unable to load currencies for the selected date. Please try a different date.');
      
      // Clear currencies and selected currency on error
      setCurrencies([]);
      setSelectedCurrency('');
    } finally {
      setIsLoadingCurrencies(false);
    }
  };

  // Real API call function to service-b
  const callConversionAPI = async (endpoint: string, data: any): Promise<ConversionResult> => {
    const response = await fetch(`http://localhost:8082/api/conversions/${endpoint}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data)
    });

    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
    }

    const result = await response.json();
    return { result: result.result };
  };

  const handleConvertToManat = async () => {
    if (!selectedDate || !amount || !selectedCurrency) {
      alert('Please fill in all required fields');
      return;
    }

    setIsLoading(true);
    setConversionType('toManat');
    
    try {
      const response = await callConversionAPI('toManat', {
        date: selectedDate,
        currency: selectedCurrency,
        amount: parseFloat(amount)
      });
      
      setResult(response.result);
      console.log('Convert to Manat:', {
        date: selectedDate,
        currency: selectedCurrency,
        amount: amount,
        result: response.result
      });
    } catch (error) {
      console.error('Error converting to Manat:', error);
      alert('Error occurred during conversion');
    } finally {
      setIsLoading(false);
    }
  };

  const handleConvertFromManat = async () => {
    if (!selectedDate || !amount || !selectedCurrency) {
      alert('Please fill in all required fields');
      return;
    }

    setIsLoading(true);
    setConversionType('fromManat');
    
    try {
      const response = await callConversionAPI('fromManat', {
        date: selectedDate,
        currency: selectedCurrency,
        amount: parseFloat(amount)
      });
      
      setResult(response.result);
      console.log('Convert from Manat:', {
        date: selectedDate,
        currency: selectedCurrency,
        amount: amount,
        result: response.result
      });
    } catch (error) {
      console.error('Error converting from Manat:', error);
      alert('Error occurred during conversion');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="App">
      <h1>Currency Converter</h1>
      
      <form className="converter-form">
        <div className="form-group">
          <label htmlFor="date">Date:</label>
          <input
            type="date"
            id="date"
            value={selectedDate}
            onChange={(e) => setSelectedDate(e.target.value)}
            max={maxDate}
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="currency">Currency:</label>
          <select
            id="currency"
            value={selectedCurrency}
            onChange={(e) => setSelectedCurrency(e.target.value)}
            disabled={isLoadingCurrencies}
          >
            {isLoadingCurrencies ? (
              <option value="">Loading currencies...</option>
            ) : currencies.length === 0 ? (
              <option value="">No currencies available</option>
            ) : (
              currencies.map((currency) => (
                <option key={currency.code} value={currency.code}>
                  {currency.name} ({currency.code})
                </option>
              ))
            )}
          </select>
          {currencyError && (
            <div className="error-message">
              {currencyError}
            </div>
          )}
        </div>

        <div className="form-group">
          <label htmlFor="amount">Amount:</label>
          <input
            type="number"
            id="amount"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            placeholder="Enter amount"
            step="0.01"
            min="0"
            required
          />
        </div>

        <div className="button-group">
          <button 
            type="button" 
            onClick={handleConvertToManat}
            className="convert-btn"
            disabled={isLoading || isLoadingCurrencies || !selectedCurrency}
          >
            {isLoading && conversionType === 'toManat' ? 'Converting...' : 'Convert to Manat'}
          </button>
          <button 
            type="button" 
            onClick={handleConvertFromManat}
            className="convert-btn"
            disabled={isLoading || isLoadingCurrencies || !selectedCurrency}
          >
            {isLoading && conversionType === 'fromManat' ? 'Converting...' : 'Convert from Manat'}
          </button>
        </div>
      </form>

      {result !== null && (
        <div className="result-box">
          <h3>Conversion Result</h3>
          <p className="result-amount">
            {conversionType === 'toManat' 
              ? `${amount} ${selectedCurrency} = ${result.toFixed(2)} AZN`
              : `${amount} AZN = ${result.toFixed(2)} ${selectedCurrency}`
            }
          </p>
          <p className="result-date">Date: {selectedDate}</p>
        </div>
      )}
    </div>
  );
};

export default App;
