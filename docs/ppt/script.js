const slides = Array.from(document.querySelectorAll('[data-slide]'));
const navIndex = document.getElementById('nav-index');
const navButtons = document.querySelectorAll('[data-nav]');
const toggles = document.querySelectorAll('[data-mode]');

const loginScenarios = [
  {
    id: '1차',
    queue: 'OFF',
    capacity: null,
    poll: null,
    iterations: 26.86,
    login_flow: { avg: 743.88, p95: 880.4 },
    queue_wait: { avg: 0.0, p95: 0.0 },
    login_req: { avg: 743.86, p95: 880.4 },
    cpu: { min: 98, max: 100, note: '거의 2분 내내 100%' },
  },
  {
    id: '2-2차',
    queue: 'ON',
    capacity: 10,
    poll: 1000,
    iterations: 17.8,
    login_flow: { avg: 1118.83, p95: 1288.2 },
    queue_wait: { avg: 1015.07, p95: 1023.0 },
    login_req: { avg: 103.73, p95: 270.0 },
    cpu: { min: 70, max: 80, note: '70~80%' },
  },
  {
    id: '2-3차',
    queue: 'ON',
    capacity: 10,
    poll: 1500,
    iterations: 12.4,
    login_flow: { avg: 1602.31, p95: 1640.6 },
    queue_wait: { avg: 1522.05, p95: 1510.0 },
    login_req: { avg: 80.24, p95: 147.0 },
    cpu: { min: 50, max: 60, note: '50~60%' },
  },
  {
    id: '2-4차',
    queue: 'ON',
    capacity: 8,
    poll: 1500,
    iterations: 6.5,
    login_flow: { avg: 3043.81, p95: 3148.1 },
    queue_wait: { avg: 2958.41, p95: 3025.0 },
    login_req: { avg: 85.36, p95: 164.05 },
    cpu: { min: 25, max: 35, note: '25~35%' },
  },
  {
    id: '2-5차',
    queue: 'ON',
    capacity: 12,
    poll: 1500,
    iterations: 12.52,
    login_flow: { avg: 1587.02, p95: 1674.9 },
    queue_wait: { avg: 1495.82, p95: 1518.0 },
    login_req: { avg: 91.16, p95: 177.0 },
    cpu: { min: 50, max: 60, note: '50~60%' },
  },
  {
    id: '2-6차',
    queue: 'ON',
    capacity: 15,
    poll: 1500,
    iterations: 12.61,
    login_flow: { avg: 1577.49, p95: 1646.2 },
    queue_wait: { avg: 1487.12, p95: 1517.4 },
    login_req: { avg: 90.33, p95: 173.0 },
    cpu: { min: 55, max: 65, note: '55~65%' },
  },
];

const reservationScenarioSet1 = [
  {
    id: '100 VUs',
    vus: 100,
    iterations: 16.085928,
    httpReqs: 32.171856,
    failRate: 45.0,
    http_req_duration: { avg: 2950, p95: 5000 },
    expected_response: { avg: 4390, p95: 5050 },
    iteration_duration: { avg: 5910, p95: 6180 },
  },
  {
    id: '200 VUs',
    vus: 200,
    iterations: 20.063576,
    httpReqs: 40.127152,
    failRate: 47.5,
    http_req_duration: { avg: 4270, p95: 7910 },
    expected_response: { avg: 5250, p95: 7980 },
    iteration_duration: { avg: 8550, p95: 9900 },
  },
  {
    id: '400 VUs',
    vus: 400,
    iterations: 22.196242,
    httpReqs: 44.392485,
    failRate: 49.25,
    http_req_duration: { avg: 7930, p95: 15840 },
    expected_response: { avg: 10010, p95: 16160 },
    iteration_duration: { avg: 15910, p95: 17940 },
  },
  {
    id: '800 VUs',
    vus: 800,
    iterations: 22.421158,
    httpReqs: 44.842317,
    failRate: 49.37,
    http_req_duration: { avg: 16740, p95: 31260 },
    expected_response: { avg: 18040, p95: 31760 },
    iteration_duration: { avg: 33530, p95: 35480 },
  },
];

const reservationScenarioSet2 = [
  {
    id: '개선 전',
    vus: 2000,
    iterations: 40.501171,
    httpReqs: 54.001561,
    failRate: 74.87,
    http_req_duration: { avg: 2630, p95: 5220 },
    expected_response: { avg: 79.2, p95: 67.71 },
    iteration_duration: { avg: 4080, p95: 6060 },
  },
  {
    id: '개선 후',
    vus: 2000,
    iterations: 40.617345,
    httpReqs: 67.695576,
    failRate: 59.9,
    http_req_duration: { avg: 640.94, p95: 1660 },
    expected_response: { avg: 35.13, p95: 68.88 },
    iteration_duration: { avg: 1050, p95: 1760 },
  },
];

let currentSlide = 0;
const modeState = {
  login: 'avg',
  res1: 'avg',
  res2: 'avg',
};

const elements = {
  login: {
    latencyChart: document.getElementById('latency-chart'),
    latencyMeta: document.getElementById('latency-meta'),
    iterChart: document.getElementById('iter-chart'),
    rangeChart: document.getElementById('cpu-chart'),
  },
  res1: {
    latencyChart: document.getElementById('res1-latency-chart'),
    latencyMeta: document.getElementById('res1-meta'),
    iterChart: document.getElementById('res1-iter-chart'),
    reqChart: document.getElementById('res1-req-chart'),
  },
  res2: {
    latencyChart: document.getElementById('res2-latency-chart'),
    latencyMeta: document.getElementById('res2-meta'),
    latencySummary: document.getElementById('res2-latency-summary'),
    iterationSummary: document.getElementById('res2-iteration-summary'),
  },
};

function setActiveSlide(index) {
  currentSlide = (index + slides.length) % slides.length;
  slides.forEach((slide, i) => {
    slide.classList.toggle('active', i === currentSlide);
  });
  navIndex.textContent = `${currentSlide + 1} / ${slides.length}`;
}

function createLatencyRow(item, maxValue, metrics, mode, labelBuilder) {
  const row = document.createElement('div');
  row.className = 'chart-row';

  const label = document.createElement('div');
  label.className = 'row-label';
  label.innerHTML = labelBuilder(item);

  const stack = document.createElement('div');
  stack.className = 'bar-stack';

  metrics.forEach((metric) => {
    const bar = document.createElement('div');
    bar.className = 'bar';
    const value = item[metric.key][mode];
    const percent = maxValue === 0 ? 0 : (value / maxValue) * 100;
    const fill = document.createElement('div');
    fill.className = `bar-fill ${metric.className}`;
    fill.style.width = `${Math.max(percent, value === 0 ? 0 : 3)}%`;
    fill.textContent = `${value.toFixed(2)}ms`;
    bar.appendChild(fill);
    stack.appendChild(bar);
  });

  row.appendChild(label);
  row.appendChild(stack);
  return row;
}

function renderLatencyChart(data, mode, metrics, container, labelBuilder) {
  container.innerHTML = '';
  const maxValue = Math.max(
    ...data.flatMap((item) => metrics.map((metric) => item[metric.key][mode]))
  );
  data.forEach((item) => {
    container.appendChild(createLatencyRow(item, maxValue, metrics, mode, labelBuilder));
  });
}

function renderLatencyMeta(data, container, metaBuilder) {
  container.innerHTML = '';
  data.forEach((item) => {
    const meta = metaBuilder(item);
    const card = document.createElement('div');
    card.className = 'meta-card';

    const title = document.createElement('div');
    title.className = 'meta-title';
    title.textContent = meta.title;
    if (meta.tagText) {
      const tag = document.createElement('span');
      tag.className = `tag ${meta.tagClass}`;
      tag.textContent = meta.tagText;
      title.appendChild(tag);
    }

    card.appendChild(title);
    meta.rows.forEach((row) => {
      const line = document.createElement('div');
      line.className = 'meta-row';
      line.innerHTML = `<span>${row.label}</span><strong>${row.value}</strong>`;
      card.appendChild(line);
    });

    container.appendChild(card);
  });
}

function renderIterationsChart(data, container, labelBuilder) {
  container.innerHTML = '';
  const maxValue = Math.max(...data.map((item) => item.iterations));
  data.forEach((item) => {
    const row = document.createElement('div');
    row.className = 'iter-row';

    const label = document.createElement('div');
    label.className = 'row-label';
    label.innerHTML = labelBuilder(item);

    const bar = document.createElement('div');
    bar.className = 'iter-bar';

    const fill = document.createElement('div');
    fill.className = 'iter-bar-fill';
    fill.style.width = `${(item.iterations / maxValue) * 100}%`;

    const value = document.createElement('span');
    value.textContent = item.iterations.toFixed(2);

    bar.appendChild(fill);
    bar.appendChild(value);
    row.appendChild(label);
    row.appendChild(bar);
    container.appendChild(row);
  });
}

function renderRangeChart(data, container, labelBuilder, rangeBuilder) {
  container.innerHTML = '';
  data.forEach((item) => {
    const rangeData = rangeBuilder(item);
    const row = document.createElement('div');
    row.className = 'cpu-row';

    const label = document.createElement('div');
    label.className = 'row-label';
    label.innerHTML = labelBuilder(item);

    const track = document.createElement('div');
    track.className = 'cpu-track';

    const range = document.createElement('div');
    range.className = 'cpu-range';
    range.style.left = `${rangeData.min}%`;
    range.style.width = `${Math.max(rangeData.max - rangeData.min, 4)}%`;

    const note = document.createElement('div');
    note.className = 'cpu-note';
    note.textContent = rangeData.note;

    track.appendChild(range);
    row.appendChild(label);
    row.appendChild(track);
    row.appendChild(note);

    container.appendChild(row);
  });
}

function renderHttpReqChart(data, container, labelBuilder) {
  container.innerHTML = '';
  const maxValue = Math.max(...data.map((item) => item.httpReqs));
  data.forEach((item) => {
    const row = document.createElement('div');
    row.className = 'iter-row';

    const label = document.createElement('div');
    label.className = 'row-label';
    label.innerHTML = labelBuilder(item);

    const bar = document.createElement('div');
    bar.className = 'iter-bar';

    const fill = document.createElement('div');
    fill.className = 'iter-bar-fill';
    fill.style.width = `${(item.httpReqs / maxValue) * 100}%`;

    const value = document.createElement('span');
    value.textContent = item.httpReqs.toFixed(2);

    bar.appendChild(fill);
    bar.appendChild(value);
    row.appendChild(label);
    row.appendChild(bar);
    container.appendChild(row);
  });
}

function renderMetricSummary(data, container, rowBuilder) {
  container.innerHTML = '';
  const rows = data.map((item) => rowBuilder(item));
  const maxValue = Math.max(...rows.map((row) => row.valueNumber));

  rows.forEach((rowData) => {
    const row = document.createElement('div');
    row.className = 'iter-row';

    const label = document.createElement('div');
    label.className = 'row-label';
    label.innerHTML = rowData.label;

    const bar = document.createElement('div');
    bar.className = 'summary-bar';

    const fill = document.createElement('div');
    fill.className = 'summary-bar-fill';
    fill.style.width = `${(rowData.valueNumber / maxValue) * 100}%`;

    const value = document.createElement('span');
    value.textContent = rowData.valueText;

    bar.appendChild(fill);
    bar.appendChild(value);
    row.appendChild(label);
    row.appendChild(bar);
    container.appendChild(row);
  });
}

function setMode(groupKey, mode) {
  modeState[groupKey] = mode;
  toggles.forEach((toggle) => {
    const isTarget = toggle.dataset.target === groupKey;
    if (isTarget) {
      toggle.classList.toggle('active', toggle.dataset.mode === mode);
    }
  });
  renderGroup(groupKey);
}

function renderGroup(groupKey) {
  if (groupKey === 'login') {
    renderLatencyChart(
      loginScenarios,
      modeState.login,
      [
        { key: 'login_flow', className: 'login-flow' },
        { key: 'queue_wait', className: 'queue-wait' },
        { key: 'login_req', className: 'login-req' },
      ],
      elements.login.latencyChart,
      (item) => `<strong>${item.id} (${item.queue})</strong>iterations/s ${item.iterations.toFixed(2)}`
    );
    renderLatencyMeta(loginScenarios, elements.login.latencyMeta, (item) => ({
      title: item.id,
      tagText: item.queue,
      tagClass: item.queue === 'ON' ? 'on' : 'off',
      rows: [
        { label: 'capacity', value: item.capacity ?? 'N/A' },
        { label: 'poll(ms)', value: item.poll ?? 'N/A' },
        { label: 'iterations/s', value: item.iterations.toFixed(2) },
      ],
    }));
    renderIterationsChart(
      loginScenarios,
      elements.login.iterChart,
      (item) => `<strong>${item.id}</strong>${item.queue}`
    );
    renderRangeChart(
      loginScenarios,
      elements.login.rangeChart,
      (item) => `<strong>${item.id}</strong>${item.queue}`,
      (item) => item.cpu
    );
  }

  if (groupKey === 'res1') {
    renderLatencyChart(
      reservationScenarioSet1,
      modeState.res1,
      [
        { key: 'http_req_duration', className: 'login-flow' },
        { key: 'expected_response', className: 'queue-wait' },
        { key: 'iteration_duration', className: 'login-req' },
      ],
      elements.res1.latencyChart,
      (item) => `<strong>${item.id}</strong>iterations/s ${item.iterations.toFixed(2)}`
    );
    renderLatencyMeta(reservationScenarioSet1, elements.res1.latencyMeta, (item) => ({
      title: item.id,
      tagText: null,
      tagClass: '',
      rows: [
        { label: 'VUs', value: item.vus },
        { label: 'iterations/s', value: item.iterations.toFixed(2) },
        { label: 'http_reqs/s', value: item.httpReqs.toFixed(2) },
        { label: 'http_req_failed', value: `${item.failRate.toFixed(2)}%` },
      ],
    }));
    renderIterationsChart(
      reservationScenarioSet1,
      elements.res1.iterChart,
      (item) => `<strong>${item.id}</strong>단일 예약`
    );
    renderHttpReqChart(
      reservationScenarioSet1,
      elements.res1.reqChart,
      (item) => `<strong>${item.id}</strong>http_reqs`
    );
  }

  if (groupKey === 'res2') {
    const mode = modeState.res2;
    renderLatencyChart(
      reservationScenarioSet2,
      modeState.res2,
      [
        { key: 'http_req_duration', className: 'login-flow' },
        { key: 'expected_response', className: 'queue-wait' },
        { key: 'iteration_duration', className: 'login-req' },
      ],
      elements.res2.latencyChart,
      (item) => `<strong>${item.id}</strong>iterations/s ${item.iterations.toFixed(2)}`
    );
    renderLatencyMeta(reservationScenarioSet2, elements.res2.latencyMeta, (item) => ({
      title: item.id,
      tagText: item.id === '개선 후' ? 'after' : 'before',
      tagClass: item.id === '개선 후' ? 'on' : 'off',
      rows: [
        { label: 'VUs', value: item.vus },
        {
          label: 'http_req_duration(ms)',
          value:
            mode === 'avg'
              ? item.http_req_duration.avg.toFixed(2)
              : item.http_req_duration.p95.toFixed(2),
        },
        {
          label: 'iteration_duration(ms)',
          value:
            mode === 'avg'
              ? item.iteration_duration.avg.toFixed(2)
              : item.iteration_duration.p95.toFixed(2),
        },
        { label: 'http_reqs/s', value: item.httpReqs.toFixed(2) },
      ],
    }));
    renderMetricSummary(
      reservationScenarioSet2,
      elements.res2.latencySummary,
      (item) => ({
        label: `<strong>${item.id}</strong>http_req_duration(ms)`,
        valueText: `${item.http_req_duration.avg.toFixed(2)} / ${item.http_req_duration.p95.toFixed(2)}`,
        valueNumber: item.http_req_duration.p95,
      })
    );
    renderMetricSummary(
      reservationScenarioSet2,
      elements.res2.iterationSummary,
      (item) => ({
        label: `<strong>${item.id}</strong>iteration_duration(ms)`,
        valueText: `${item.iteration_duration.avg.toFixed(2)} / ${item.iteration_duration.p95.toFixed(2)}`,
        valueNumber: item.iteration_duration.p95,
      })
    );
  }
}

navButtons.forEach((button) => {
  button.addEventListener('click', () => {
    const direction = button.dataset.nav === 'next' ? 1 : -1;
    setActiveSlide(currentSlide + direction);
  });
});

document.addEventListener('keydown', (event) => {
  if (event.key === 'ArrowRight') {
    setActiveSlide(currentSlide + 1);
  }
  if (event.key === 'ArrowLeft') {
    setActiveSlide(currentSlide - 1);
  }
});

toggles.forEach((toggle) => {
  toggle.addEventListener('click', () => {
    const target = toggle.dataset.target;
    setMode(target, toggle.dataset.mode);
  });
});

setActiveSlide(0);
renderGroup('login');
renderGroup('res1');
renderGroup('res2');
